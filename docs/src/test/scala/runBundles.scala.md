
```scala
package era7bio.db.test

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._

import era7bio.db._, rnaCentralCompats._
import era7.defaults._

case object runBundles {

  // use `sbt test:console`:
  // > era7bio.db.test.bundles.runBundle(...)
  def runBundle[B <: AnyBundle](compat: DefaultCompatible[B], user: AWSUser): List[String] =
    EC2.create(user.profile)
      .runInstances(
        amount = 1,
        compat.instanceSpecs(
          c3.x2large,
          user.keypair.name,
          Some(ec2Roles.projects.name)
        )
      )
      .map { _.getInstanceId }
}

```




[main/scala/blastDB.scala]: ../../main/scala/blastDB.scala.md
[main/scala/csvUtils.scala]: ../../main/scala/csvUtils.scala.md
[main/scala/rnaCentral.scala]: ../../main/scala/rnaCentral.scala.md
[test/scala/18sitsdatabase.scala]: 18sitsdatabase.scala.md
[test/scala/compats.scala]: compats.scala.md
[test/scala/runBundles.scala]: runBundles.scala.md