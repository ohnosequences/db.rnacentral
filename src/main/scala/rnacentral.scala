/*
  ## RNACentral data

  We mirror RNACentral data at S3. There are important differences across versions: for example, the fields in the taxid mappings are different.
*/
package ohnosequences.db.rnacentral

import ohnosequences.db.Row
import ohnosequences.cosas._, types._, records._
import ohnosequences.awstools._, s3._

sealed trait RNAcentralField extends AnyType {
  type Raw = String
  lazy val label = toString
}

case object RNAcentralField {

  case object id          extends RNAcentralField
  case object db          extends RNAcentralField
  case object external_id extends RNAcentralField
  case object tax_id      extends RNAcentralField
  // TODO use http://www.insdc.org/rna_vocab.html
  case object rna_type    extends RNAcentralField
  case object gene_name   extends RNAcentralField
}

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

case object RNAcentral6 extends AnyRNAcentral("6.0") {
  import RNAcentralField._

  case object Id2Taxa extends RecordType(
    id          :×:
    db          :×:
    external_id :×:
    tax_id      :×:
    rna_type    :×:
    gene_name   :×:
    |[RNAcentralField]
  )

  def row(fields: Seq[String]): Row = Row(Id2Taxa.keys.types.asList, fields)
}

case object RNAcentral7 extends AnyRNAcentral("7.0") {
  import RNAcentralField._

  case object Id2Taxa extends RecordType(
    id          :×:
    db          :×:
    external_id :×:
    tax_id      :×:
    rna_type    :×:
    |[RNAcentralField]
  )

  def row(fields: Seq[String]): Row = Row(Id2Taxa.keys.types.asList, fields)
}
