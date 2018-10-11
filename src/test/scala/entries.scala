package ohnosequences.db.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.test._
import ohnosequences.db.rnacentral._
import org.scalatest.EitherValues._

class Entries extends FunSuite {

  test("parse all", ReleaseOnlyTest) {

    Version.all foreach { v =>
      data.rnacentralData(v).left.map { err =>
        println(err.msg)
      }
      val (malformedRows, parsedRows) =
        entries.entriesFrom(data.rnacentralData(v).right.value)

      assert { malformedRows.isEmpty && allRight { parsedRows } }
    }
  }
}
