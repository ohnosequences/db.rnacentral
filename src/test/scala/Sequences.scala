package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.db.rnacentral._
import ohnosequences.test._

class Sequences extends FunSuite {

  test("parsing and integrity", ReleaseOnlyTest) {

    data.fastas(Version.latest) forall {
      case (id, fastas) =>
        fastas.nonEmpty &&
          (fastas forall { sequences.fasta.rnaID(_) == id })
    }
  }

  test("idempotent parsing/serialization", ReleaseOnlyTest) {

    // parse and serialize
    def parseAndSerializeAndParse =
      sequences.rnaIDAndSequenceDataFrom(
        (sequences sequenceAnnotationsAndSequence data.rnacentralData(
          Version.latest))
          .map { x =>
            (x._1, sequences.seqDataToFASTAs(x).toSeq)
          }
      )

    (sequences.sequenceAnnotationsAndSequence(data.rnacentralData(
      Version.latest)) zip parseAndSerializeAndParse) foreach {
      case (x1, x2) =>
        assert { x1 == x2 }
    }
  }
}
