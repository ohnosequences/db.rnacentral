package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.test._
import ohnosequences.db.rnacentral._

class Entries extends FunSuite {

  test("parse all", ReleaseOnlyTest) {

    Version.all foreach { v =>
      val (malformedRows, parsedRows) =
        entries entriesFrom data.rnacentralData(v)

      assert { allRight { malformedRows.isEmpty && allRight { parsedRows } } }
    }
  }
}
