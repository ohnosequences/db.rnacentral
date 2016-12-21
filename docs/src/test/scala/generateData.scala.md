
```scala
package ohnosequences.db.rnacentral.test

import ohnosequences.test._
import era7bio.defaults._

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

```




[main/scala/blastDB.scala]: ../../main/scala/blastDB.scala.md
[main/scala/collectionUtils.scala]: ../../main/scala/collectionUtils.scala.md
[main/scala/csvUtils.scala]: ../../main/scala/csvUtils.scala.md
[main/scala/filterData.scala]: ../../main/scala/filterData.scala.md
[main/scala/rnacentral.scala]: ../../main/scala/rnacentral.scala.md
[test/scala/compats.scala]: compats.scala.md
[test/scala/generateData.scala]: generateData.scala.md
[test/scala/rnaCentral.scala]: rnaCentral.scala.md
[test/scala/runBundles.scala]: runBundles.scala.md