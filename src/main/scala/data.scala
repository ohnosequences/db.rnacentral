package ohnosequences.db.rnacentral

import ohnosequences.awstools._, s3._

case object data {

  val version: String =
    "7.0"

  case object input {

    val baseURL: String =
    "ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral"
  
    val releaseURL: String =
      s"${baseURL}/releases/${version}"

    val idMappingTSV: String =
      "id_mapping.tsv"

    val idMappingTSVGZ: String =
      s"${idMappingTSV}.gz"
    
    val idMappingTSVGZURL: String =
      s"${releaseURL}/id_mapping/${idMappingTSVGZ}"

    val speciesSpecificFASTA: String =
      "rnacentral_species_specific_ids.fasta"

    val speciesSpecificFASTAGZ: String =
      s"${speciesSpecificFASTA}.gz"

    val speciesSpecificFASTAGZURL =
      s"${releaseURL}/sequences/${speciesSpecificFASTAGZ}"
  }

  val prefix =
    s3"resources.ohnosequences.com" /
      "db"                          /
      "rnacentral"                  /
      version                       /

  val idMappingTSV: S3Object =
    prefix / input.idMappingTSV

  val speciesSpecificFASTA: S3Object =
    prefix / input.speciesSpecificFASTA
}