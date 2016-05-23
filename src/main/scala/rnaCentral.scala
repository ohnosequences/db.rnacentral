package era7bio.db

import ohnosequences.cosas._, types._, records._, klists._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._
import ohnosequences.statika._, aws._
import ohnosequences.fastarious._, fasta._

import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

import better.files._

import com.github.tototoshi.csv._
import csvUtils._

/*
  ## RNACentral data

  We mirror RNACentral data at S3. There are important differences across versions: for example, the fields in the taxid mappings are different.
*/
abstract class AnyRNACentral(val version: String) {

  lazy val prefix = S3Object("resources.ohnosequences.com","")/"rnacentral"/version/

  val fastaFileName:       String = s"rnacentral.${version}.fasta"
  val tableFileName:       String = s"table.${version}.tsv"

  lazy val fasta:       S3Object = prefix/fastaFileName
  lazy val table:       S3Object = prefix/tableFileName
}

case object RNACentral5 extends AnyRNACentral("5.0") {

  sealed trait Field extends AnyType {
    type Raw = String
    lazy val label = toString
  }

  case object id          extends Field
  case object db          extends Field
  case object external_id extends Field
  case object tax_id      extends Field
  // TODO use http://www.insdc.org/rna_vocab.html
  case object rna_type    extends Field
  case object gene_name   extends Field


  case object Id2Taxa extends RecordType(
    id          :×:
    db          :×:
    external_id :×:
    tax_id      :×:
    rna_type    :×:
    gene_name   :×:
    |[Field]
  )
}

/*
  ### Mirror RNACentral release files in S3

  This bundle

  1. downloads all RNACentral raw files from the EBI ftp
  2. creates other table file containing only the *active* sequences (those actually found in RNACentral)
  3. creates a 2-column table with id to taxas mapping (only active ids)
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
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

      // upload the uncompressed fasta file
      transferManager.upload(
        rnaCentral.fasta.bucket, rnaCentral.fasta.key,
        fastaFile.toJava
      ).waitForCompletion

      // upload full table file
      transferManager.upload(
        rnaCentral.table.bucket, rnaCentral.table.key,
        tableFile.toJava
      ).waitForCompletion
    } -&-
    say(s"RNACentral version ${rnaCentral.version} mirrored at ${rnaCentral.prefix} including active-only table mapping")
  }
}

// bundle:
case object MirrorRNAcentral5 extends MirrorRNAcentral(RNACentral5)
