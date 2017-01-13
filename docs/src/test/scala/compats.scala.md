
```scala
package ohnosequences.db.rnacentral.test

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions._, ec2._, autoscaling._, s3._

case object compats {

  class DefaultCompatible[B <: AnyBundle](bundle: B, javaHeap: Int = 10
```

G

```scala
) extends Compatible(
    amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore), javaHeap),
    bundle,
    ohnosequences.db.generated.metadata.rnacentral
  )

  case object MirrorRNAcentral extends DefaultCompatible(ohnosequences.db.rnacentral.test.MirrorRNAcentral)
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