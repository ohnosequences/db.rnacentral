package ohnosequences.api.rnacentral.test

import ohnosequences.api.rnacentral._
import java.io.File

object testData extends RNACentralData(
  speciesSpecificFasta = 
    new File("/opt/data/rnacentral_species_specific_ids.fasta"),
  idMapping =
    new File("/opt/data/id_mapping.tsv")
)