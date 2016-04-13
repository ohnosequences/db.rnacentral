
```scala
package era7bio.db

import ohnosequences.cosas._, types._, klists._
import ohnosequences.statika._
import ohnosequences.fastarious.fasta._
import ohnosequences.blast.api._
import ohnosequences.awstools.s3._

import ohnosequencesBundles.statika.Blast

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import com.github.tototoshi.csv._

import better.files._

import csvUtils._, RNACentral5._


trait AnyBlastDB {
  val dbType: BlastDBType

  val name: String

  val predicate: (Row, FASTA.Value) => Boolean

  val rnaCentralRelease: AnyRNACentral
  private[db] lazy val sourceFasta: S3Object = rnaCentralRelease.fasta
  private[db] lazy val sourceTable: S3Object = rnaCentralRelease.id2taxaactive

  val s3location: S3Folder
}


case object blastBundle extends Blast("2.2.31")


trait AnyGenerateBlastDB extends AnyBundle {

  type BlastDB <: AnyBlastDB
  val db: BlastDB

  // Files
  lazy val sources = file"sources/"
  lazy val outputs = file"outputs/"

  lazy val sourceFasta: File = sources / "source.fasta"
  lazy val sourceTable: File = sources / "source.table.tsv"

  lazy val outputFasta:   File = outputs / s"${db.name}.fasta"
  lazy val outputTable:   File = outputs / "id2taxa.tsv"
  lazy val outputBlastDB: File = outputs / "blastdb"

  def instructions: AnyInstructions = {

    val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

    LazyTry {
      println(s"""Downloading the sources...
        |fasta: ${db.sourceFasta}
        |table: ${db.sourceTable}
        |""".stripMargin)

      transferManager.download(
        db.sourceFasta.bucket, db.sourceFasta.key,
        sourceFasta.toJava
      ).waitForCompletion

      transferManager.download(
        db.sourceTable.bucket, db.sourceTable.key,
        sourceTable.toJava
      ).waitForCompletion
    } -&-
    LazyTry {
      println("Processing sources...")

      processSources(
        sourceTable,
        outputTable
      )(sourceFasta,
        outputFasta
      )
    } -&-
    seqToInstructions(
      makeblastdb(
        argumentValues =
          in(outputFasta) ::
          input_type(DBInputType.fasta) ::
          dbtype(db.dbType) ::
          *[AnyDenotation],
        optionValues =
          title(db.name) ::
          *[AnyDenotation]
      ).toSeq
    ) -&-
    LazyTry {
      println("Uploading the DB...")

      // Moving blast DB files to a separate folder
      outputBlastDB.createDirectory()
      outputs.list
        .filter{ _.name.startsWith(s"${outputFasta.name}.") }
        .foreach { f => f.moveTo(outputBlastDB / f.name) }

      // Uploading all together
      transferManager.uploadDirectory(
        db.s3location.bucket, db.s3location.key,
        outputs.toJava,
        true // includeSubdirectories
      ).waitForCompletion
    } -&-
    say(s"The database is uploaded to [${db.s3location}]")
  }

  // This is the main processing part, that is separate to facilitate local testing
  final def processSources(
    tableInFile: File,
    tableOutFile: File
  )(fastaInFile: File,
    fastaOutFile: File
  ) {
    tableOutFile.createIfNotExists()
    fastaOutFile.createIfNotExists()

    val tableReader = CSVReader.open(tableInFile.toJava)(tableFormat)
    val tableWriter = CSVWriter.open(tableOutFile.toJava, append = true)(tableFormat)

    val rows: Iterator[Row] = tableReader.iterator
    val fastas: Iterator[FASTA.Value] = parseFastaDropErrors(fastaInFile.lines)

    // NOTE: here we rely on that the sources are prefiltered and don't have duplicate ID
    (rows zip fastas)
      .filter { case (row, fasta) => db.predicate(row, fasta) }
      .foreach { case (row, fasta) =>

        val rowID = row.select(id)
        val fastaID = fasta.getV(header).id

        if (rowID != fastaID)
          sys.error(s"Table row ID [${rowID}] doesn't match FASTA ID [${fastaID}]!")

        val extID = s"${rowID}|lcl|${db.name}"

        tableWriter.writeRow(List(
          extID,
          row.select(tax_id)
        ))

        fastaOutFile.appendLine(
          fasta.update(
            header := FastaHeader(s"${extID} ${fasta.getV(header).description}")
          ).asString
        )
      }

    tableReader.close()
    tableWriter.close()
  }

}

class GenerateBlastDB[DB <: AnyBlastDB](val db: DB)
  extends Bundle(blastBundle)
  with AnyGenerateBlastDB { type BlastDB = DB }


// This bundle downloads a BlastDB and provides interface for using it.
// It uses generation bundle as a reference to know the exact filenames.
trait AnyBlastDBRelease extends AnyBundle {

  type Generated <: AnyGenerateBlastDB
  val generated: Generated

  type BlastDB = Generated#BlastDB
  val db: BlastDB = generated.db

  lazy val destination: File = File(db.s3location.key)

  lazy val id2taxa:    File = destination / generated.outputTable.name
  // This is where the BLAST DB will be downloaded
  lazy val dbLocation: File = destination / generated.outputBlastDB.name
  // This is what you pass to BLAST
  lazy val dbName:     File = dbLocation / generated.outputFasta.name

  def instructions: AnyInstructions = {
    LazyTry {
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

      transferManager.downloadDirectory(
        db.s3location.bucket, db.s3location.key,
        file".".toJava
      ).waitForCompletion
    } -&-
    say(s"Reference database ${db.name} was dowloaded to ${destination.path}")
  }
}

class BlastDBRelease[G <: AnyGenerateBlastDB](val generated: G)
  extends Bundle()
  with AnyBlastDBRelease { type Generated = G }

```




[main/scala/blastDB.scala]: blastDB.scala.md
[main/scala/csvUtils.scala]: csvUtils.scala.md
[main/scala/rnaCentral.scala]: rnaCentral.scala.md
[test/scala/18sitsdatabase.scala]: ../../test/scala/18sitsdatabase.scala.md
[test/scala/compats.scala]: ../../test/scala/compats.scala.md
[test/scala/runBundles.scala]: ../../test/scala/runBundles.scala.md