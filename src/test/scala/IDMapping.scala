package ohnosequences.api.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.api.rnacentral._

class IDMapping extends FunSuite {

  test("well-formed tsv") {

    assert { allRight( IDMapping rows testData ) }
  }

  test("parse all EntryAnnotations") {

    assert { 
      allRight {
        IDMapping entryAnnotations {
          (IDMapping rows testData) collect { case Right(row) => row }
        }
      }
    }
  }
}