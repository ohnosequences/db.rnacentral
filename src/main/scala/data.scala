package ohnosequences.db.rnacentral

import ohnosequences.awstools._, s3._

sealed abstract class Version(val name: String) {
  override final def toString: String = name
}
object Version {

  lazy val latest: Version = _9_0

  val all: Set[Version] =
    Set(_9_0, _7_0)

  case object _9_0 extends Version("9.0")
  case object _8_0 extends Version("8.0")
  case object _7_0 extends Version("7.0")
}

case object data {

  case object input {

    final val baseURL: String =
      "ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral"

    def releaseURL(version: Version): String =
      s"${baseURL}/releases/${version}"

    def idMappingTSV: String =
      "id_mapping.tsv"

    def idMappingTSVGZ: String =
      s"${idMappingTSV}.gz"

    def idMappingTSVGZURL(version: Version): String =
      s"${releaseURL(version)}/id_mapping/${idMappingTSVGZ}"

    def speciesSpecificFASTA: String =
      "rnacentral_species_specific_ids.fasta"

    def speciesSpecificFASTAGZ: String =
      s"${speciesSpecificFASTA}.gz"

    def speciesSpecificFASTAGZURL(version: Version): String =
      s"${releaseURL(version)}/sequences/${speciesSpecificFASTAGZ}"
  }

  def prefix(version: Version): S3Folder =
    s3"resources.ohnosequences.com" /
      "ohnosequences" /
      "db" /
      "rnacentral" /
      version.toString /

  def idMappingTSV(version: Version): S3Object =
    prefix(version) / input.idMappingTSV

  def speciesSpecificFASTA(version: Version): S3Object =
    prefix(version) / input.speciesSpecificFASTA

  def everything(version: Version): Set[S3Object] =
    Set(idMappingTSV(version), speciesSpecificFASTA(version))
}
