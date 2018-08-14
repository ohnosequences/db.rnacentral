package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.test._
import ohnosequences.db.rnacentral._

class IDMapping extends FunSuite {

  test("well-formed tsv", ReleaseOnlyTest) {

    assert { allRight(IDMapping rows data) }
  }

  test("parse all EntryAnnotations", ReleaseOnlyTest) {

    assert {
      allRight {
        IDMapping entryAnnotations {
          iterators right (IDMapping rows data)
        }
      }
    }
  }
}
