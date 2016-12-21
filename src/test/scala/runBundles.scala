package ohnosequences.db.rnacentral.test

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, regions._, ec2._, autoscaling._, s3._
import com.amazonaws.services.ec2.model.{ Instance => _ , _ }
import scala.collection.JavaConversions._
import ohnosequences.db._
import era7bio.defaults._

case object rnacentral {

  def defaultSpecs[B <: AnyBundle](
    compat: compats.DefaultCompatible[B],
    user: AWSUser
  ) = compat.instanceSpecs(
    c3.large,
    user.keypair.name,
    Some(ec2Roles.projects.name)
  )

  // use `sbt test:console`:
  // > ohnosequences.db.test.bundles.runBundle(...)
  def runBundle[B <: AnyBundle](
    user: AWSUser,
    compat: compats.DefaultCompatible[B]
  ): Option[Instance] =
    EC2Client(credentials = user.profile)
      .runInstances(defaultSpecs(compat, user))(1)
      .toOption
      .flatMap { _.headOption }

  /* This runs a bundle  */
  def launchAndMonitor[B <: AnyBundle](
    user: AWSUser,
    compat: compats.DefaultCompatible[B],
    terminateOnSuccess: Boolean
  ): Either[String, String] = {

    runBundle(user, compat).map { inst =>

      def checkStatus: String = inst.ec2.describeTags(
        new DescribeTagsRequest(List(
          new Filter("resource-type", List("instance")),
          new Filter("resource-id", List(inst.id)),
          new Filter("key", List("statika-status"))
        ))
      ).getTags
        .headOption
        .map { _.getValue }
        .getOrElse("...")

      val id = inst.getInstanceId()
      def printStatus(st: String) = println(s"${compat.toString} (${id}): ${st}")

      printStatus("launched")

      while(checkStatus != "preparing") { Thread sleep 2000 }
      printStatus(s"url: ${inst.publicDNS}")

      @annotation.tailrec
      def waitForCompletion(previous: String): String = {
        val current = checkStatus
        if(current == "failure" || current == "success") {
          printStatus(current)
          current
        } else {
          if (current != previous) printStatus(current)
          Thread sleep 3000
          waitForCompletion(current)
        }
      }

      if (waitForCompletion(checkStatus) != "success") {
        Left(s"Bundle launch has failed. Instance ${id} is left running for you to check logs.")
      } else {
        if (terminateOnSuccess) { inst.terminate }
        Right(s"Bundle launch finished successfully.")
      }
    }
    .getOrElse(Left("Couldn't launch an instance. Check your AWS credentials."))
  }

}
