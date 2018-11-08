package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.db.rnacentral._
import ohnosequences.test._
import org.scalatest.EitherValues._

class Sequences extends FunSuite {

  test("parsing and integrity", ReleaseOnlyTest) {
    Version.all foreach { version =>
      data.fastas(version).right.value foreach {
        case (id, fastas) =>
          assert {
            fastas.nonEmpty && (fastas forall { sequences.fasta.rnaID(_) == id })
          }
      }
    }
  }

  test("idempotent parsing/serialization", ReleaseOnlyTest) {

    // parse and serialize
    def parseAndSerializeAndParse(rnacentralData: RNACentralData) =
      sequences.rnaIDAndSequenceDataFrom(
        (sequences sequenceAnnotationsAndSequence rnacentralData)
          .map { x =>
            (x._1, sequences.seqDataToFASTAs(x).toSeq)
          }
      )

    Version.all foreach { version =>
      val rnacentralData = data.rnacentralData(version).right.value

      val original  = sequences.sequenceAnnotationsAndSequence(rnacentralData)
      val processed = parseAndSerializeAndParse(rnacentralData)
      (original zip processed) foreach {
        case (x1, x2) =>
          assert { x1 == x2 }
      }
    }
  }
}
