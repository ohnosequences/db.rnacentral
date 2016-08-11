package ohnosequences.db.rnacentral.test

import ohnosequences.test._
import era7.defaults._

class GenerateDataTests extends org.scalatest.FunSuite {

  val user = awsUsers.aalekhin

  test("Run RNAcentral mirroring and wait for the result", ReleaseOnlyTest) {

    rnacentral.launchAndMonitor(
      user,
      compats.MirrorRNAcentral5,
      terminateOnSuccess = true
    ).fold(
      { msg => fail(msg) },
      { msg => info(msg); assert(true) }
    )
  }
}
