package era7bio.db

import ohnosequences.cosas._, types._, klists._
import ohnosequences.statika._
import ohnosequences.awstools.s3._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._


/* Each filtering bundle is defined by its input data, two output S3 folders and the filtering method.
   It downloads the sources, filters and uploads two kinds of results: accepted and rejected data.
   If you need to chain different filtering steps, just do it through bundle dependencies.
*/
abstract class FilterData(
  val sourceTableS3: S3Object,
  val sourceFastaS3: S3Object,
  val acceptedS3Prefix: S3Folder,
  val rejectedS3Prefix: S3Folder
)(deps: AnyBundle*) extends Bundle(deps.toSeq: _*) {


  /* Names of the source/accepted/rejected files are the same as of the input S3 objects */
  final lazy val tableName: String = sourceTableS3.key.split('/').last
  final lazy val fastaName: String = sourceFastaS3.key.split('/').last

  /* Each folder has two files inside */
  case class folder(prefix: String) {
    lazy val asFile: File = File(prefix).createDirectories()

    lazy val table: File = (asFile / tableName).createIfNotExists()
    lazy val fasta: File = (asFile / fastaName).createIfNotExists()
  }

  lazy val source = folder("sources")
  lazy val accepted = folder("outputs/accepted")
  lazy val rejected = folder("outputs/rejected")


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
        source.table.toJava
      ).waitForCompletion

      transferManager.download(
        sourceFastaS3.bucket, sourceFastaS3.key,
        source.fasta.toJava
      ).waitForCompletion
    } -&-
    LazyTry {
      println("Filtering the data...")

      filterData()
    } -&-
    LazyTry {
      println("Uploading the results...")

      transferManager.uploadDirectory(
        acceptedS3Prefix.bucket, acceptedS3Prefix.key,
        accepted.asFile.toJava,
        false // don't includeSubdirectories
      ).waitForCompletion

      transferManager.uploadDirectory(
        rejectedS3Prefix.bucket, rejectedS3Prefix.key,
        rejected.asFile.toJava,
        false // don't includeSubdirectories
      ).waitForCompletion
    } -&-
    say(s"Filtered data is uploaded to [${acceptedS3Prefix}] and [${rejectedS3Prefix}]")
  }

}
