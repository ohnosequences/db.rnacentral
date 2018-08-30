package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.test._
import ohnosequences.db.rnacentral._

class Entries extends FunSuite {

  test("parse all", ReleaseOnlyTest) {

    val (malformedRows, parsedRows) = entries entriesFrom data

    assert { malformedRows.isEmpty }
    assert { allRight { parsedRows } }
  }
}
