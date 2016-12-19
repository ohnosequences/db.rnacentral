package ohnosequences.db.rnacentral.test

import ohnosequences.cosas._, types._, records._, klists._
import ohnosequences.awstools._, regions._, ec2._, autoscaling._, s3._
import ohnosequences.statika._, aws._
import ohnosequences.fastarious._, fasta._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._

import com.github.tototoshi.csv._
import ohnosequences.db.csvUtils._
import ohnosequences.db.rnacentral._

/*
  ### Mirror RNACentral release files in S3

  This bundle

  1. downloads all RNACentral raw files from the EBI ftp
  4. uploads everything to S3
*/
class MirrorRNAcentral[R <: AnyRNACentral](r: R) extends Bundle() {

  type RNACentral = R
  val rnaCentral: RNACentral = r

  lazy val dataFolder = file"/media/ephemeral0"

  lazy val fastaFile = dataFolder/"rnacentral_active.fasta"
  lazy val tableFile = dataFolder/"id_mapping.tsv"

  lazy val getRnaCentralFastaFileGz = cmd("wget")(
    s"ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral/releases/${rnaCentral.version}/sequences/${fastaFile.name}.gz"
  )
  lazy val getRnaCentralIdMappingGz = cmd("wget")(
    s"ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral/releases/${rnaCentral.version}/id_mapping/${tableFile.name}.gz"
  )

  def instructions: AnyInstructions = {
    // get raw input stuff from EBI FTP
    getRnaCentralFastaFileGz -&-
    cmd("gzip")("-d", s"${fastaFile.name}.gz") -&-
    getRnaCentralIdMappingGz -&-
    cmd("gzip")("-d", s"${tableFile.name}.gz") -&-
    LazyTry {
      println("Uploading uncompressed data...")
      val transferManager = new TransferManager(new DefaultAWSCredentialsProviderChain())

      // upload the uncompressed fasta file
      transferManager.upload(
        rnaCentral.fasta.bucket, rnaCentral.fasta.key,
        fastaFile.toJava
      ).waitForCompletion
      println("Uploaded fasta file.")

      // upload full table file
      transferManager.upload(
        rnaCentral.table.bucket, rnaCentral.table.key,
        tableFile.toJava
      ).waitForCompletion
      println("Uploaded table file.")

      transferManager.shutdownNow()
      println("Shutdown the transfer manager.")
    } -&-
    say(s"RNACentral version ${rnaCentral.version} mirrored at ${rnaCentral.prefix}")
  }
}

// bundle:
case object MirrorRNAcentral5 extends MirrorRNAcentral(RNACentral5)
