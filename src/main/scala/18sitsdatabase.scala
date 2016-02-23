package era7bio.db

import ohnosequences.statika._
import ohnosequences.awstools._, s3._, regions._
import better.files._
import com.amazonaws.auth._
import com.amazonaws.services.s3.transfer._

/*
  ### RNAcentral sequences

  We have RNA sequences mirrored at S3, scoped by version.
*/
abstract class AnyRNAcentral(val version: String) {

  lazy val prefix = S3Object("resources.ohnosequences.com","")/"rna-central"/version/

  val fastaFileName         : String = s"rnacentral.${version}.fasta"
  val idTaxaMappingFileName : String = s"id2taxa.${version}.tsv"

  def fasta         : S3Object = prefix/fastaFileName
  def idTaxaMapping : S3Object = prefix/idTaxaMappingFileName
}

case object RNAcentral extends AnyRNAcentral("4.0")
//
trait AnyRNAcentralRelease extends AnyBundle {

  val rnaCentral: AnyRNAcentral
  val dataFolder = File("/media/ephemeral")

  lazy val rnaCentralFastaFile          = dataFolder/rnaCentral.fastaFileName
  lazy val rnaCentralIdTaxaMappingFile  = dataFolder/rnaCentral.idTaxaMappingFileName

  def instructions: AnyInstructions = LazyTry {

    val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

    // get the fasta file
    transferManager.download(
      rnaCentral.fasta.bucket, rnaCentral.fasta.key,
      rnaCentralFastaFile.toJava
    )
    .waitForCompletion

    // get the id file
    transferManager.download(
      rnaCentral.idTaxaMapping.bucket, rnaCentral.idTaxaMapping.key,
      rnaCentralIdTaxaMappingFile.toJava
    )
    .waitForCompletion

    // now try to build the fasta stuff
  }
}
