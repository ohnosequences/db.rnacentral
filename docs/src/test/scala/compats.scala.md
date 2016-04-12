
```scala
package era7bio.db

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._

case object rnaCentralCompats {

  class DefaultCompatible[B <: AnyBundle](b: B) extends Compatible(
    amznAMIEnv(
      AmazonLinuxAMI(Ireland, HVM, InstanceStore),
      javaHeap = 20 // in G
    ),
    b,
    generated.metadata.rnacentraldb
  )

  // compatible:
  case object MirrorRNAcentral5Compat extends DefaultCompatible(MirrorRNAcentral5)
}

```




[main/scala/blastDB.scala]: ../../main/scala/blastDB.scala.md
[main/scala/csvUtils.scala]: ../../main/scala/csvUtils.scala.md
[main/scala/rnaCentral.scala]: ../../main/scala/rnaCentral.scala.md
[test/scala/18sitsdatabase.scala]: 18sitsdatabase.scala.md
[test/scala/compats.scala]: compats.scala.md
[test/scala/runBundles.scala]: runBundles.scala.md