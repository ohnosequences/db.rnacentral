package ohnosequences.api.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.api.rnacentral._
import ohnosequences.test._, testData._

class Sequences extends FunSuite {

  test("parsing and integrity", ReleaseOnlyTest) {

    testSequences forall {
      case (id, fastas) =>
        fastas.nonEmpty &&
        (fastas forall { sequences.fasta.rnaID(_) == id })
    }
  }

  test("idempotent parsing/serialization", ReleaseOnlyTest) {

    // parse and serialize
    def parseAndSerializeAndParse =
      sequences.rnaIDAndSequenceDataFrom(
        (sequences sequenceAnnotationsAndSequence testData)
          .map { x => (x._1, sequences.seqDataToFASTAs(x).toSeq) }
      )

    (sequences.sequenceAnnotationsAndSequence(testData) zip parseAndSerializeAndParse) foreach { case (x1, x2) =>
      assert { x1 == x2 }
    }
  }
}