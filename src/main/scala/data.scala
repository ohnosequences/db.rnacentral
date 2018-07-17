package ohnosequences.db.rnacentral

import ohnosequences.awstools._, s3._

case object data {

  lazy val version: String =
    "9.0"

  case object input {

    lazy val baseURL: String =
      "ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral"

    lazy val releaseURL: String =
      s"${baseURL}/releases/${version}"

    lazy val idMappingTSV: String =
      "id_mapping.tsv"

    lazy val idMappingTSVGZ: String =
      s"${idMappingTSV}.gz"

    lazy val idMappingTSVGZURL: String =
      s"${releaseURL}/id_mapping/${idMappingTSVGZ}"

    lazy val speciesSpecificFASTA: String =
      "rnacentral_species_specific_ids.fasta"

    lazy val speciesSpecificFASTAGZ: String =
      s"${speciesSpecificFASTA}.gz"

    lazy val speciesSpecificFASTAGZURL =
      s"${releaseURL}/sequences/${speciesSpecificFASTAGZ}"
  }

  lazy val prefix =
    s3"resources.ohnosequences.com" /
      "ohnosequences" /
      "db" /
      "rnacentral" /
      version /

  lazy val idMappingTSV: S3Object =
    prefix / input.idMappingTSV

  lazy val speciesSpecificFASTA: S3Object =
    prefix / input.speciesSpecificFASTA
}
