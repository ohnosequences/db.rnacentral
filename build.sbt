name          := "db.rnacentral"
organization  := "ohnosequences"
description   := "Mirror RNACentral data"
bucketSuffix  := "era7.com"

crossScalaVersions := Seq("2.11.11", "2.12.4")
scalaVersion := crossScalaVersions.value.max

resolvers := Seq(
  "Era7 private maven releases" at s3("private.releases.era7.com").toHttps(s3region.value.toString)
) ++ resolvers.value

libraryDependencies ++= Seq(
  "ohnosequences" %% "aws-scala-tools" % "0.20.0"
) ++ testDependencies

val testDependencies = Seq(
  "era7bio"       %% "defaults"   % "0.3.0-RC3" % Test,
  "org.scalatest" %% "scalatest"  % "3.0.4"     % Test,
  "ohnosequences" %% "statika"    % "3.0.0"     % Test
)

// wartremoverErrors in (Test, compile) := Seq()

// shows time for each test:
testOptions in Test += Tests.Argument("-oD")
// disables parallel exec
parallelExecution in Test := false

generateStatikaMetadataIn(Test)

// This includes tests sources in the assembled fat-jar:
fullClasspath in assembly := (fullClasspath in Test).value

// This turns on fat-jar publishing during release process:
publishFatArtifact in Release := true
