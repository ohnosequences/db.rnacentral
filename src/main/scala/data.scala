package ohnosequences.db.rnacentral

import ohnosequences.s3.{S3Object, s3AddressFromString}
import ohnosequences.files.digest.DigestFunction
import java.io.File

sealed abstract class Version(val name: String) {
  override final def toString: String = name
}
object Version {

  lazy val all: Set[Version] =
    Set(_10_0)

  case object _10_0 extends Version("10.0")
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

  case object local {

    def idMappingFile(version: Version, localFolder: File): File =
      new File(localFolder, input.idMappingTSV)

    def idMappingGZFile(version: Version, localFolder: File): File =
      new File(localFolder, input.idMappingTSVGZ)

    def fastaFile(version: Version, localFolder: File): File =
      new File(localFolder, input.speciesSpecificFASTA)

    def fastaGZFile(version: Version, localFolder: File): File =
      new File(localFolder, input.speciesSpecificFASTAGZ)
  }

  def prefix(version: Version): String => S3Object =
    file =>
      s3"resources.ohnosequences.com" / "ohnosequences" / "db" / "rnacentral" / "unstable" / version.toString / file

  def idMappingTSV(version: Version): S3Object =
    prefix(version)(input.idMappingTSV)

  def speciesSpecificFASTA(version: Version): S3Object =
    prefix(version)(input.speciesSpecificFASTA)

  def everything(version: Version): Set[S3Object] =
    Set(idMappingTSV(version), speciesSpecificFASTA(version))

  val hashingFunction: DigestFunction = DigestFunction.SHA512
}
