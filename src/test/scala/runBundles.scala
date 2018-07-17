package ohnosequences.db.rnacentral.test

import ohnosequences.statika._, aws._
import ohnosequences.awstools._, ec2._
import com.amazonaws.services.ec2.model.{Instance => _, _}
import scala.collection.JavaConverters._
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
    ec2.defaultClient
      .runInstances(defaultSpecs(compat, user))(1)
      .toOption
      .flatMap { _.headOption }

  /* This runs a bundle  */
  def launchAndMonitor[B <: AnyBundle](
      user: AWSUser,
      compat: compats.DefaultCompatible[B],
      terminateOnSuccess: Boolean
  ): Either[String, String] =
    runBundle(user, compat)
      .map { inst =>
        def checkStatus: String =
          inst.ec2
            .describeTags(
              new DescribeTagsRequest(
                List(
                  new Filter("resource-type", List("instance").asJava),
                  new Filter("resource-id", List(inst.id).asJava),
                  new Filter("key", List("statika-status").asJava)
                ).asJava)
            )
            .getTags
            .asScala
            .headOption
            .map { _.getValue }
            .getOrElse("...")

        val id = inst.getInstanceId()
        def printStatus(st: String) =
          println(s"${compat.toString} (${id}): ${st}")

        printStatus("launched")

        while (checkStatus != "preparing") { Thread sleep 2000 }
        printStatus(s"url: ${inst.publicDNS}")

        @annotation.tailrec
        def waitForCompletion(previous: String): String = {
          val current = checkStatus
          if (current == "failure" || current == "success") {
            printStatus(current)
            current
          } else {
            if (current != previous) printStatus(current)
            Thread sleep 3000
            waitForCompletion(current)
          }
        }

        if (waitForCompletion(checkStatus) != "success") {
          Left(
            s"Bundle launch has failed. Instance ${id} is left running for you to check logs.")
        } else {
          if (terminateOnSuccess) { inst.terminate }
          Right(s"Bundle launch finished successfully.")
        }
      }
      .getOrElse(Left(
        "Couldn't launch an instance. Check your AWS credentials."))

}
