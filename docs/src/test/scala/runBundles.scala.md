
```scala
package era7bio.db.test

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._

import era7bio.db._
import era7.defaults._

case object rnacentral {

  // use `sbt test:console`:
  // > era7bio.db.test.bundles.runBundle(...)
  def runBundle[B <: AnyBundle](compat: era7bio.db.rnacentral.compats.DefaultCompatible[B], user: AWSUser): List[String] =
    EC2.create(user.profile)
      .runInstances(
        amount = 1,
        compat.instanceSpecs(
          c3.large,
          user.keypair.name,
          Some(ec2Roles.projects.name)
        )
      )
      .map { _.getInstanceId }
}

```




[main/scala/blastDB.scala]: ../../main/scala/blastDB.scala.md
[main/scala/collectionUtils.scala]: ../../main/scala/collectionUtils.scala.md
[main/scala/csvUtils.scala]: ../../main/scala/csvUtils.scala.md
[main/scala/filterData.scala]: ../../main/scala/filterData.scala.md
[main/scala/rnacentral/compats.scala]: ../../main/scala/rnacentral/compats.scala.md
[main/scala/rnacentral/rnaCentral.scala]: ../../main/scala/rnacentral/rnaCentral.scala.md
[test/scala/runBundles.scala]: runBundles.scala.md