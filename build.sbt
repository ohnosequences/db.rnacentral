name          := "db.rnacentral"
organization  := "ohnosequences"
description   := "Mirror and preprocessing of RNACentral data"

bucketSuffix  := "era7.com"

resolvers := Seq(
  "Era7 private maven releases"  at s3("private.releases.era7.com").toHttps(s3region.value.toString)
) ++ resolvers.value

libraryDependencies ++= Seq(
  "ohnosequences" %% "fastarious" % "0.8.0",
  "ohnosequences" %% "blast-api"  % "0.8.0",
  "ohnosequences-bundles" %% "blast"     % "0.4.0",
  "com.github.tototoshi"  %% "scala-csv" % "1.3.4",
  "com.github.pathikrit" %% "better-files" % "2.16.0",
  // Test only:
  "era7bio" %% "defaults" % "0.3.0-RC2" % Test
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
