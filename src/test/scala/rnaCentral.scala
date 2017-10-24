package ohnosequences.db.rnacentral.test

import ohnosequences.cosas._, records._
import ohnosequences.statika._, aws._
import ohnosequences.awstools._, s3._
import better.files._
import ohnosequences.db.rnacentral._

/*
  ### Mirror RNACentral release files in S3

  This bundle

  1. downloads all RNACentral raw files from the EBI ftp
  4. uploads everything to S3
*/
class MirrorRNAcentral[R <: AnyRNAcentral](r: R) extends Bundle() {

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
      lazy val s3client = s3.defaultClient

      // upload the uncompressed fasta file
      s3client.upload(fastaFile.toJava, rnaCentral.fasta)
      println("Uploaded fasta file.")

      // upload full table file
      s3client.upload(tableFile.toJava, rnaCentral.table)
      println("Uploaded table file.")

      println("Shutdown the transfer manager.")
    } -&-
    say(s"RNACentral version ${rnaCentral.version} mirrored at ${rnaCentral.prefix}")
  }
}

// bundle:
case object MirrorRNAcentral extends MirrorRNAcentral(RNAcentral)
