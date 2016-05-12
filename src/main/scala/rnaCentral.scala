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
  val tableActiveFileName: String = s"table.active.${version}.tsv"
  val id2taxasFileName:    String = s"id2taxas.active.${version}.tsv"

  lazy val fasta:       S3Object = prefix/fastaFileName
  lazy val table:       S3Object = prefix/tableFileName
  lazy val tableActive: S3Object = prefix/tableActiveFileName
  lazy val id2taxas:    S3Object = prefix/id2taxasFileName
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

  // inputs:
  lazy val rnaCentralFastaFile = dataFolder/"rnacentral_active.fasta"
  lazy val tableFile           = dataFolder/"id_mapping.tsv"
  // outputs:
  lazy val tableActiveFile = dataFolder/rnaCentral.tableActiveFileName
  lazy val id2taxasFile    = dataFolder/rnaCentral.id2taxasFileName

  lazy val getRnaCentralFastaFileGz = cmd("wget")(
    s"ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral/releases/${rnaCentral.version}/sequences/${rnaCentralFastaFile.name}.gz"
  )
  lazy val getRnaCentralIdMappingGz = cmd("wget")(
    s"ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral/releases/${rnaCentral.version}/id_mapping/${tableFile.name}.gz"
  )

  def instructions: AnyInstructions = {
    // get raw input stuff from EBI FTP
    getRnaCentralFastaFileGz -&-
    cmd("gzip")("-d", s"${rnaCentralFastaFile.name}.gz") -&-
    getRnaCentralIdMappingGz -&-
    cmd("gzip")("-d", s"${tableFile.name}.gz") -&-
    LazyTry {

      val fastaIDs: Set[String] = fasta
        .parseFastaDropErrors(rnaCentralFastaFile.lines)
        .map{ _.getV(header).id }
        .toSet

      val tableReader = CSVReader.open(tableFile.toJava)(tableFormat)

      val tableActiveWriter = CSVWriter.open(tableActiveFile.toJava, append = true)(tableFormat)
      val id2taxasWriter    = CSVWriter.open(id2taxasFile.toJava,    append = true)(tableFormat)

      // TODO: check that all fastaIDs are present in the table?
      // errLogFile << s"${fa.getV(header)} not found in ${id2taxaFile}. All subsequent will fail"

      import RNACentral5._

      tableReader.iterator.toStream
        .groupBy { _.select(id) }
        .foreach { case (id, rows) =>
          if (fastaIDs.contains(id)) {

            // writing all rows for this ID to the active table
            rows.foreach { tableActiveWriter.writeRow }

            // writing ID with all taxIDs corresponding to it (in one column)
            val taxas = rows.map{ _.select(tax_id) }.distinct.mkString("; ")
            id2taxasWriter.writeRow(Seq(id, taxas))

          } else {

            // TODO: write these inactive ids somewhere?
            println(s"Skipping inactive ID: ${id}")
          }
        }

      tableReader.close()
      tableActiveWriter.close()
      id2taxasWriter.close()
    } -&-
    LazyTry {
      val transferManager = new TransferManager(new InstanceProfileCredentialsProvider())

      // upload the uncompressed fasta file
      transferManager.upload(
        rnaCentral.fasta.bucket, rnaCentral.fasta.key,
        rnaCentralFastaFile.toJava
      ).waitForCompletion

      // upload full table file
      transferManager.upload(
        rnaCentral.table.bucket, rnaCentral.table.key,
        tableFile.toJava
      ).waitForCompletion

      // upload active table file
      transferManager.upload(
        rnaCentral.tableActive.bucket, rnaCentral.tableActive.key,
        tableActiveFile.toJava
      ).waitForCompletion

      // upload id2taxas file
      transferManager.upload(
        rnaCentral.id2taxas.bucket, rnaCentral.id2taxas.key,
        id2taxasFile.toJava
      ).waitForCompletion
    } -&-
    say(s"RNACentral version ${rnaCentral.version} mirrored at ${rnaCentral.prefix} including active-only table mapping")
  }
}

// bundle:
case object MirrorRNAcentral5 extends MirrorRNAcentral(RNACentral5)
