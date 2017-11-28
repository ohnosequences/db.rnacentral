package ohnosequences.api.rnacentral.test

import org.scalatest.FunSuite
import ohnosequences.test._
import ohnosequences.api.rnacentral._

class Entries extends FunSuite {

  test("parse all", ReleaseOnlyTest) {

    assert { allRight { entries entriesFrom testData } }
  }
}