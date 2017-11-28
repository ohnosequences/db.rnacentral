package ohnosequences.api.rnacentral.test

import ohnosequences.api.rnacentral._
import java.io.File

object testData extends RNACentralData(
  speciesSpecificFasta = 
    new File("/opt/data/rnacentral_species_specific_ids.fasta"),
  idMapping =
    new File("/opt/data/id_mapping.tsv")
)
{

  def testSequences =
    sequences fastaByRNAID testData

  def testEntryAnnotations =
    iterators.segmentsFrom[(RNAID, EntryAnnotation), RNAID]({ case (id, _) => id }) {
      IDMapping.entryAnnotations(
        (IDMapping rows testData) collect { case Right(z) => z }
      ) collect { case Right(z) => z }
    }
}