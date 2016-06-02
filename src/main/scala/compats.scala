package era7bio.db

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._

case object rnaCentralCompats {

  class DefaultCompatible[B <: AnyBundle](bundle: B, javaHeap = 10 /*G*/) extends Compatible(
    amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore), javaHeap),
    bundle,
    generated.metadata.db.rnacentral
  )

  case object MirrorRNAcentral5Compat extends DefaultCompatible(MirrorRNAcentral5)
}
