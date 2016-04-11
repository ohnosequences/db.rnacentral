package era7bio.db

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._
import era7.defaults._, loquats._

case object runRNACentralBundles {

  class DefaultCompatible[B <: AnyBundle](b: B) extends Compatible(
    amznAMIEnv(
      AmazonLinuxAMI(Ireland, HVM, InstanceStore),
      javaHeap = 20 // in G
    ),
    b,
    generated.metadata.Rnacentraldb
  )

  case object MirrorRNAcentralReleaseCompat extends DefaultCompatible(MirrorRNAcentralRelease)

  def runBundle[B <: AnyBundle](compat: DefaultCompatible[B], user: AWSUser): List[String] =
    EC2.create(user.profile)
      .runInstances(
        amount = 1,
        compat.instanceSpecs(
          c3.x2large,
          user.keypair.name,
          Some(ec2Roles.projects.name)
        )
      ).map { inst =>

        val id = inst.getInstanceId
        println(s"Launched [${id}]")
        id
      }
}
