package ohnosequences.db.rnacentral.test

import ohnosequences.test._
import era7.defaults._

class GenerateDataTests extends org.scalatest.FunSuite {


  test("Run RNAcentral mirroring and wait for the result", ReleaseOnlyTest) {

    assert {
      rnacentral.launchAndWait(compats.MirrorRNAcentral5, awsUsers.aalekhin)
    }
  }
}
