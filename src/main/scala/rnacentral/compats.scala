package era7bio.db.rnacentral

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._

case object compats {

  class DefaultCompatible[B <: AnyBundle](bundle: B, javaHeap: Int = 10 /*G*/) extends Compatible(
    amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore), javaHeap),
    bundle,
    generated.metadata.db.rnacentral
  )

  case object MirrorRNAcentral5 extends DefaultCompatible(era7bio.db.rnacentral.MirrorRNAcentral5)
}
