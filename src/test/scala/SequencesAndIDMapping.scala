package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.test._

class SequencesAndIDMapping extends FunSuite {

  test("FASTA and ID mapping files are aligned", ReleaseOnlyTest) {

    (testSequences zip testEntryAnnotations) forall {
      case ((id1, fastas), (id2, annots)) =>
        (id1 == id2) &&
          (fastas.length <= annots.size)
    }
  }
}
