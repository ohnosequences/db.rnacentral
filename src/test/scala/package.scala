package ohnosequences.db.rnacentral

import java.io.File

package object test {

  val data: RNACentralData =
    RNACentralData(
      speciesSpecificFasta =
        new File("/opt/data/rnacentral_species_specific_ids.fasta"),
      idMapping = new File("/opt/data/id_mapping.tsv")
    )

  def testSequences =
    sequences fastaByRNAID data

  def testEntryAnnotations =
    IDMapping entryAnnotationsByRNAID {
      iterators right {
        IDMapping entryAnnotations (
          iterators right (IDMapping rows data)
        )
      }
    }

  def allRight[X, Y]: Iterator[X + Y] => Boolean =
    _.forall {
      case Left(_)  => false
      case Right(_) => true
    }
}
