package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.db.rnacentral._
import ohnosequences.test._

class Sequences extends FunSuite {

  test("parsing and integrity", ReleaseOnlyTest) {
    Version.all foreach { version =>
      data.fastas(version) forall {
        case (id, fastas) =>
          fastas.nonEmpty && (fastas forall { sequences.fasta.rnaID(_) == id })
      }
    }
  }

  test("idempotent parsing/serialization", ReleaseOnlyTest) {

    // parse and serialize
    def parseAndSerializeAndParse(version: Version) =
      sequences.rnaIDAndSequenceDataFrom(
        (sequences sequenceAnnotationsAndSequence data.rnacentralData(version))
          .map { x =>
            (x._1, sequences.seqDataToFASTAs(x).toSeq)
          }
      )

    Version.all foreach { version =>
      (sequences.sequenceAnnotationsAndSequence(data.rnacentralData(version)) zip parseAndSerializeAndParse(
        version)) foreach {
        case (x1, x2) =>
          assert { x1 == x2 }
      }
    }
  }
}
