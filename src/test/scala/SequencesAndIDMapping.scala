package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.test._
import ohnosequences.db.rnacentral.Version

class SequencesAndIDMapping extends FunSuite {

  test("FASTA and ID mapping files are aligned", ReleaseOnlyTest) {

    Version.all foreach { v =>
      (data.fastas(v) zip data.annotations(v)) forall {
        case ((id1, fastas), (id2, annots)) =>
          (id1 == id2) &&
            (fastas.length <= annots.size)
      }
    }
  }
}
