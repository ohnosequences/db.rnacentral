
```scala
package ohnosequences.db.rnacentral

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._

case object compats {

  class DefaultCompatible[B <: AnyBundle](bundle: B, javaHeap: Int = 10
```

G

```scala
) extends Compatible(
    amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore), javaHeap),
    bundle,
    generated.metadata.db.rnacentral
  )

  case object MirrorRNAcentral5 extends DefaultCompatible(ohnosequences.db.rnacentral.MirrorRNAcentral5)
}

```




[test/scala/runBundles.scala]: ../../../test/scala/runBundles.scala.md
[main/scala/filterData.scala]: ../filterData.scala.md
[main/scala/csvUtils.scala]: ../csvUtils.scala.md
[main/scala/collectionUtils.scala]: ../collectionUtils.scala.md
[main/scala/rnacentral/rnaCentral.scala]: rnaCentral.scala.md
[main/scala/rnacentral/compats.scala]: compats.scala.md
[main/scala/blastDB.scala]: ../blastDB.scala.md