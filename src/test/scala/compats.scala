package ohnosequences.db.rnacentral.test

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions._, ec2._, autoscaling._, s3._

case object compats {

  class DefaultCompatible[B <: AnyBundle](bundle: B, javaHeap: Int = 10 /*G*/) extends Compatible(
    amznAMIEnv(AmazonLinuxAMI(Ireland, HVM, InstanceStore), javaHeap),
    bundle,
    ohnosequences.db.generated.metadata.rnacentral
  )

  case object MirrorRNAcentral extends DefaultCompatible(ohnosequences.db.rnacentral.test.MirrorRNAcentral)
}
