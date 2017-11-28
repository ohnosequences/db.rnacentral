package ohnosequences.api.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.api.rnacentral._
import ohnosequences.test._, testData._

class Sequences extends FunSuite {

  test("parsing and integrity", ReleaseOnlyTest) {

    testSequences forall {
      case (id, fastas) =>
        fastas.nonEmpty &&
        (fastas forall { fa => sequences.rnaIDFromFASTA(fa) == id })
    }
  }
}