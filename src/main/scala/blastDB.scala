package ohnosequences.db

import ohnosequences.cosas._, types._, klists._
import ohnosequences.statika._
import ohnosequences.blast.api._
import ohnosequences.awstools.s3._

import ohnosequencesBundles.statika.Blast

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

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
  final lazy val s3: S3Folder = s3prefix / "blastdb" /

  lazy val sources = file"sources/"
  lazy val outputs = file"outputs/"

  lazy val sourceFastaFile: File = sources / s"${dbName}.fasta"

  def instructions: AnyInstructions = {

    val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

    LazyTry {
      println(s"""Downloading the sources...
        |fasta: ${sourceFastaS3}
        |""".stripMargin)

      transferManager.download(
        sourceFastaS3.bucket, sourceFastaS3.key,
        sourceFastaFile.toJava
      ).waitForCompletion

      println("Generating BLAST DB...")
    } -&-
    seqToInstructions(
      makeblastdb(
        argumentValues =
          in(sourceFastaFile) ::
          input_type(DBInputType.fasta) ::
          dbtype(dbType) ::
          *[AnyDenotation],
        optionValues =
          title(dbName) ::
          *[AnyDenotation]
      ).toSeq ++ Seq("-parse_seqids") // TODO use blast-api after updating
    ) -&-
    LazyTry {
      println("Uploading the DB...")

      // Moving blast DB files to the outputs/ folder
      outputs.createDirectory()
      sources.list
        .filter { _.name.startsWith(sourceFastaFile.name + ".") }
        .foreach { f => f.moveTo(outputs / f.name) }

      // Uploading outputs
      transferManager.uploadDirectory(
        s3.bucket, s3.key,
        outputs.toJava,
        false // includeSubdirectories
      ).waitForCompletion
    } -&-
    say(s"The database is uploaded to [${s3}]")
  }

}

class FilterAndGenerateBlastDB(
  dbName: String,
  dbType: BlastDBType,
  filterData: FilterData
) extends GenerateBlastDB(
  dbName,
  dbType,
  sourceFastaS3 = filterData.output.fasta.s3,
  s3prefix      = filterData.s3
)(deps = filterData)
