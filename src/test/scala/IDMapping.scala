package ohnosequences.api.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.test._
import ohnosequences.api.rnacentral._

class IDMapping extends FunSuite {

  test("well-formed tsv", ReleaseOnlyTest) {

    assert { allRight( IDMapping rows testData ) }
  }

  test("parse all EntryAnnotations", ReleaseOnlyTest) {

    assert { 
      allRight {
        IDMapping entryAnnotations {
           iterators right (IDMapping rows testData)
        }
      }
    }
  }
}