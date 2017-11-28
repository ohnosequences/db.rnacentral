package ohnosequences.db.rnacentral.test

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions._, ec2._

case object compats {

  class DefaultCompatible[B <: AnyBundle](bundle: B, javaHeap: Int = 10 /*G*/) extends Compatible(
    amznAMIEnv(
      amazonAMI = AmazonLinuxAMI(Ireland, HVM, InstanceStore),
      javaHeap = javaHeap
    ),
    bundle,
    ohnosequences.db.generated.metadata.rnacentral
  )

  case object mirrorRNAcentral extends DefaultCompatible(ohnosequences.db.rnacentral.test.mirrorRNAcentral)
}
