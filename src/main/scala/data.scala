package ohnosequences.db.rnacentral

import ohnosequences.s3.{S3Object, s3AddressFromString}
import ohnosequences.files.digest.DigestFunction
import java.io.File

/**
  * Encapsulates all information attached to a specific version of the data
  *
  * @param name is the name of the version, that must match the name of an
  * RNACentral release; i.e., the name of one of the directories in
  * ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral/releases/
  */
sealed abstract class Version(val name: String) {
  override final def toString: String = name
}

object Version {

  lazy val all: Set[Version] =
    Set(_9_0, _10_0)

  /**
    * RNACentral release 9.
    * See http://blog.rnacentral.org/2018/04/rnacentral-release-9.html
    */
  case object _9_0 extends Version("9.0")
  type _9_0 = _9_0.type

  /**
    * RNACentral release 10.
    * http://blog.rnacentral.org/2018/08/rnacentral-release-10.html
    */
  case object _10_0 extends Version("10.0")
  type _10_0 = _10_0.type
}

case object data {

  /**
    * Values and methods to retrieve the input files from RNACentral FTP
    */
  case object input {

    final val baseURL: String =
      "ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral"

    def releaseURL(version: Version): String =
      s"${baseURL}/releases/${version}"

    /**
      * Name of the id mapping file
      */
    val idMappingTSV: String = "id_mapping.tsv"

    /**
      * Name of the gzipped id mapping file
      */
    val idMappingTSVGZ: String = s"${idMappingTSV}.gz"

    /**
      * FTP path of the gzipped id mapping file from RNACentral version `version`
      */
    def idMappingTSVGZURL(version: Version): String =
      s"${releaseURL(version)}/id_mapping/${idMappingTSVGZ}"

    /**
      * Name of the fasta file
      */
    val speciesSpecificFASTA: String = "rnacentral_species_specific_ids.fasta"

    /**
      * Name of the gzipped fasta file
      */
    val speciesSpecificFASTAGZ: String = s"${speciesSpecificFASTA}.gz"

    /**
      * FTP path of the gzipped fasta file from RNACentral version `version`
      */
    def speciesSpecificFASTAGZURL(version: Version): String =
      s"${releaseURL(version)}/sequences/${speciesSpecificFASTAGZ}"
  }

  case object local {

    /**
      * Local file used when downloading the id mapping file from RNACentral FTP
      *
      * @param localFolder is the directory where the file will be created
      */
    def idMappingFile(localFolder: File): File =
      new File(localFolder, input.idMappingTSV)

    /**
      * Local file used when downloading the gzipped id mapping from RNACentral
      * FTP
      *
      * @param localFolder is the directory where the file will be created
      */
    def idMappingGZFile(localFolder: File): File =
      new File(localFolder, input.idMappingTSVGZ)

    /**
      * Local file used when downloading the fasta file from RNACentral FTP
      *
      * @param localFolder is the directory where the file will be created
      */
    def fastaFile(localFolder: File): File =
      new File(localFolder, input.speciesSpecificFASTA)

    /**
      * Local file used when downloading the gzipped fasta file from RNACentral
      * FTP
      *
      * @param localFolder is the directory where the file will be created
      */
    def fastaGZFile(localFolder: File): File =
      new File(localFolder, input.speciesSpecificFASTAGZ)
  }

  /**
    * Generator of S3 objects in a directory parametrized by a version.
    *
    * @param version is the version of the db.rnacentral data whose paths we
    * want to obtain.
    *
    * @return a function `String => S3Object` that, given the name of a file,
    * return an S3 object in a fixed S3 directory, which is parametrized by the
    * version passed.
    */
  def prefix(version: Version): String => S3Object =
    file =>
      s3"resources.ohnosequences.com" /
        "ohnosequences" /
        "db" /
        "rnacentral" /
        "unstable" /
        version.toString /
      file

  /**
    * Return the path of the S3 object containing the mirrored id mapping for
    * the version passed
    */
  def idMappingTSV(version: Version): S3Object =
    prefix(version)(input.idMappingTSV)

  /**
    * Return the path of the S3 object containing the mirrored fasta for the
    * version passed.
    */
  def speciesSpecificFASTA(version: Version): S3Object =
    prefix(version)(input.speciesSpecificFASTA)

  /**
    * A set of all the S3 objects generated in the version passed
    */
  def everything(version: Version): Set[S3Object] =
    Set(idMappingTSV(version), speciesSpecificFASTA(version))

  /**
    * The function used to hash the content of the files that are uploaded to S3
    */
  val hashingFunction: DigestFunction = DigestFunction.SHA512
}
