package ohnosequences.db

import ohnosequences.cosas._, types._, klists._
import ohnosequences.statika._
import ohnosequences.blast.api._
import ohnosequences.awstools._, s3._
import ohnosequencesBundles.statika.Blast
import better.files._


case object blastBundle extends Blast("2.2.31")


/* This bundle takes source data from the given place, generates BlastDB and
   uploads it to S3. It _does not_ do any filtering, it should be handled by
   other bundles which can be listed as dependencies.
*/
abstract class GenerateBlastDB(
  val dbName: String,
  val dbType: BlastDBType,
  val sourceFastaS3: S3Object,
  val s3prefix: S3Folder
)(deps: AnyBundle*) extends Bundle(blastBundle +: deps.toSeq: _*) {

  // where the generated files will be uploaded
  final lazy val s3destination: S3Folder = s3prefix / "blastdb" /

  lazy val sources = file"sources/"
  lazy val outputs = file"outputs/"

  lazy val sourceFastaFile: File = sources / s"${dbName}.fasta"

  def instructions: AnyInstructions = {
    lazy val s3client = s3.defaultClient

    LazyTry {
      println(s"""Downloading the sources...
        |fasta: ${sourceFastaS3}
        |""".stripMargin
      )
      s3client.download(sourceFastaS3, sourceFastaFile.toJava)

      outputs.createDirectory()
      println("Generating BLAST DB...")
    } -&-
    seqToInstructions(
      makeblastdb(
        argumentValues =
          in(sourceFastaFile.toJava) ::
          input_type(DBInputType.fasta) ::
          dbtype(dbType) ::
          out(outputs.toJava) ::
          *[AnyDenotation],
        optionValues = makeblastdb.defaults.update(
          title(dbName) ::
          parse_seqids(true) ::
          *[AnyDenotation]
        ).value
      ).toSeq
    ) -&-
    LazyTry {
      println("Uploading the DB...")
      s3client.upload(outputs.toJava, s3destination)
    } -&-
    say(s"The database is uploaded to [${s3destination}]")
  }

}

class FilterAndGenerateBlastDB(
  dbName: String,
  filterData: FilterData
) extends GenerateBlastDB(
  dbName,
  dbType        = BlastDBType.nucl,
  sourceFastaS3 = filterData.output.fasta.s3,
  s3prefix      = filterData.s3
)(deps = filterData)
