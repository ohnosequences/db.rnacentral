package ohnosequences.db.rnacentral.test

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions.Region._, ec2._, InstanceType._, autoscaling._, s3._

import ohnosequences.db._
import era7.defaults._

case object rnacentral {

  def defaultSpecs[B <: AnyBundle](
    compat: compats.DefaultCompatible[B],
    user: AWSUser
  ) = //: InstanceSpecs =
    compat.instanceSpecs(
      c3.large,
      user.keypair.name,
      Some(ec2Roles.projects.name)
    )

  // use `sbt test:console`:
  // > ohnosequences.db.test.bundles.runBundle(...)
  def runBundle[B <: AnyBundle](
    compat: compats.DefaultCompatible[B],
    user: AWSUser
  ): List[String] =
    EC2.create(user.profile)
      .runInstances(1, defaultSpecs(compat, user))
      .map { _.getInstanceId }

  /* This runs a bundle  */
  def launchAndWait[B <: AnyBundle](
    compat: compats.DefaultCompatible[B],
    user: AWSUser
  ): Boolean = {

    EC2.create(user.profile)
      .runInstances(1, defaultSpecs(compat, user))
      .headOption
      .map { inst =>

        def checkStatus: String = inst.getTagValue("statika-status").getOrElse("...")

        val id = inst.getInstanceId()
        def printStatus(st: String) = println(s"${compat.toString} (${id}): ${st}")

        printStatus("launched")

        while(checkStatus != "preparing") { Thread sleep 2000 }
        printStatus("url: "+inst.getPublicDNS().getOrElse("..."))

        @annotation.tailrec
        def waitForSuccess(previous: String): String = {
          val current = checkStatus
          if(current == "failure" || current == "success") {
            printStatus(current)
            current
          } else {
            if (current != previous) printStatus(current)
            Thread sleep 3000
            waitForSuccess(current)
          }
        }

        waitForSuccess(checkStatus) == "success"
      }
      .getOrElse(false)
  }

}
