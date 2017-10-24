name          := "db.rnacentral"
organization  := "ohnosequences"
description   := "Mirror and preprocessing of RNACentral data"
bucketSuffix  := "era7.com"

crossScalaVersions := Seq("2.11.11", "2.12.3")
scalaVersion := crossScalaVersions.value.max

resolvers := Seq(
  "Era7 private maven releases"  at s3("private.releases.era7.com").toHttps(s3region.value.toString)
) ++ resolvers.value

libraryDependencies ++= Seq(
  "ohnosequences" %% "aws-scala-tools" % "0.20.0",
  "ohnosequences" %% "fastarious" % "0.12.0",
  "ohnosequences" %% "blast-api"  % "0.11.1",
  "ohnosequences-bundles" %% "blast"     % "0.5.0",
  "com.github.tototoshi"  %% "scala-csv" % "1.3.5",
  "com.github.pathikrit" %% "better-files" % {
    scalaBinaryVersion.value match {
      case "2.11" => "2.16.0"
      case _      => "3.2.0"
    }
  },
  // Test only:
  "era7bio" %% "defaults" % "0.3.0-RC3" % Test
)

dependencyOverrides ++= Seq(
  // scala-csv is bound to scala 2.12.2, check this after updating scala-logging
  "org.scala-lang" % "scala-library" % scalaVersion.value
)

wartremoverErrors in (Test, compile) := Seq()
wartremoverErrors in (Compile, compile) := Seq()

// shows time for each test:
testOptions in Test += Tests.Argument("-oD")
// disables parallel exec
parallelExecution in Test := false

generateStatikaMetadataIn(Compile)

// This includes tests sources in the assembled fat-jar:
fullClasspath in assembly := (fullClasspath in Test).value

// This turns on fat-jar publishing during release process:
publishFatArtifact in Release := true
