package era7bio.db

import ohnosequences.cosas._, types._, klists._
import ohnosequences.statika._
import ohnosequences.awstools.s3._
import ohnosequences.fastarious.fasta._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import com.github.tototoshi.csv._
import better.files._


/* Each filtering bundle is defined by its input data, two output S3 folders and the filtering method.
   It downloads the sources, filters and uploads two kinds of results: accepted and rejected data.
   If you need to chain different filtering steps, just do it through bundle dependencies.
*/
abstract class FilterData(
  val sourceTableS3: S3Object,
  val sourceFastaS3: S3Object,
  val s3prefix: S3Folder // of the database
)(deps: AnyBundle*) extends Bundle(deps.toSeq: _*) { filter =>

  lazy val name: String = toString

  final lazy val s3: S3Folder = s3prefix / name /
  final lazy val tableName: String = name + ".csv"
  final lazy val fastaName: String = name + ".fasta"

  /* Each folder has two files inside */

  /* Source folder provides ways to read the table and stream FASTA */
  case object source { folder =>
    lazy val file: File = file"source".createDirectories()

    case object table {
      lazy val file = folder.file / tableName

      // use either of these two (RNAcentral table is TSV, all the outputs are CSV)
      lazy val tsvReader = CSVReader.open(this.file.toJava)(csvUtils.RNAcentralTSVFormat)
      lazy val csvReader = CSVReader.open(this.file.toJava)(csvUtils.UnixCSVFormat)
    }

    case object fasta {
      lazy val file = folder.file / fastaName
      lazy val stream: Stream[FASTA.Value] = parseFastaDropErrors(this.file.lines).toStream
    }
  }

  /* Output folder knows how to write to the files and where they will be uploaded */
  case object output { folder =>
    lazy val file: File   = File(folder.toString).createDirectories()
    lazy val s3: S3Folder = filter.s3 / folder.toString /

    case object table {
      lazy val file: File   = (folder.file / tableName).createIfNotExists()
      lazy val s3: S3Object = folder.s3 / tableName

      lazy val writer = CSVWriter.open(this.file.toJava, append = true)(csvUtils.UnixCSVFormat)

      def add(id: String, accepted: Seq[String]): Unit =
        writer.writeRow(Seq( id, accepted.mkString(";") ))
    }

    case object fasta {
      lazy val file: File = (folder.file / fastaName).createIfNotExists()
      lazy val s3: S3Object = folder.s3 / fastaName

      def add(fasta: FASTA.Value): Unit = file.appendLine(fasta.asString)
    }
  }

  /* Summary folder contains a table with all accepted/rejected assignments after this filter */
  case object summary { folder =>
    lazy val file: File   = File(folder.toString).createDirectories()
    lazy val s3: S3Folder = filter.s3 / folder.toString /

    case object table {
      lazy val file: File   = (folder.file / tableName).createIfNotExists()
      lazy val s3: S3Object = folder.s3 / tableName

      lazy val writer = CSVWriter.open(this.file.toJava, append = true)(csvUtils.UnixCSVFormat)

      def add(id: String, accepted: Seq[String], rejected: Seq[String]) =
        writer.writeRow(Seq(
          id,
          accepted.mkString(";"),
          rejected.mkString(";")
        ))
    }
  }

  // a shortcut for writing both output and summary
  def writeOutput(
    id: String,
    acceptedTaxas: Seq[String],
    rejectedTaxas: Seq[String],
    fasta: FASTA.Value
  ) = {
    summary.table.add(id, acceptedTaxas, rejectedTaxas)

    if (acceptedTaxas.nonEmpty) {
      output.table.add(id, acceptedTaxas)
      output.fasta.add(fasta)
    }
  }

  /* Implementing this method you define the filter.
     It should refer to the folders/files defined above. */
  def filterData(): Unit


  def instructions: AnyInstructions = {

    val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

    LazyTry {
      println(s"""Downloading the sources...
        |table: ${sourceTableS3}
        |fasta: ${sourceFastaS3}
        |""".stripMargin)

      transferManager.download(
        sourceTableS3.bucket, sourceTableS3.key,
        source.table.file.toJava
      ).waitForCompletion

      transferManager.download(
        sourceFastaS3.bucket, sourceFastaS3.key,
        source.fasta.file.toJava
      ).waitForCompletion
    } -&-
    LazyTry {
      println("Filtering the data...")

      summary.table.writer.writeRow(Seq("Sequence ID", "Accepted Taxas", "Rejected Taxas"))

      filterData()

      source.table.tsvReader.close()
      source.table.csvReader.close()

      output.table.writer.close()
      summary.table.writer.close()
    } -&-
    LazyTry {
      println("Uploading the results...")

      transferManager.uploadDirectory(
        output.s3.bucket, output.s3.key,
        output.file.toJava,
        false // don't includeSubdirectories
      ).waitForCompletion

      transferManager.uploadDirectory(
        summary.s3.bucket, summary.s3.key,
        summary.file.toJava,
        false // don't includeSubdirectories
      ).waitForCompletion
    } -&-
    say(s"Filtered data is uploaded to [${output.s3}] and [${summary.s3}]")
  }

}

abstract class FilterDataFrom(previousFilter: FilterData)(deps: AnyBundle*)
extends FilterData(
  sourceTableS3 = previousFilter.output.table.s3,
  sourceFastaS3 = previousFilter.output.fasta.s3,
  s3prefix      = previousFilter.s3prefix
)(deps: _*)
