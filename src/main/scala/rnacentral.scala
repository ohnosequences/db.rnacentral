/*
  ## RNACentral data

  We mirror RNACentral data at S3. There are important differences across versions: for example, the fields in the taxid mappings are different.
*/
package ohnosequences.db.rnacentral

import ohnosequences.cosas._, types._, records._
import ohnosequences.awstools._, s3._

abstract class AnyRNAcentral(val version: String) {

  val metadata = ohnosequences.db.generated.metadata.rnacentral

  lazy val prefix = s3"resources.ohnosequences.com" /
    metadata.organization /
    metadata.artifact /
    metadata.version /

  val fastaFileName: String = s"rnacentral.${version}.fasta"
  val tableFileName: String = s"table.${version}.tsv"

  lazy val fasta: S3Object = prefix / fastaFileName
  lazy val table: S3Object = prefix / tableFileName
}

case object RNAcentral extends AnyRNAcentral("6.0") {

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
