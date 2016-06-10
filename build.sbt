Nice.scalaProject

name          := "db.rnacentral"
organization  := "era7bio"
description   := "Code generation and bundle for a 18S RNA databases"

GithubRelease.repo := organization.value +"/"+ name.value

scalaVersion := "2.11.8"

bucketSuffix  := "era7.com"

resolvers := Seq(
  "Era7 private maven releases"  at s3("private.releases.era7.com").toHttps(s3region.value.toString),
  "Era7 private maven snapshots" at s3("private.snapshots.era7.com").toHttps(s3region.value.toString)
) ++ resolvers.value

libraryDependencies ++= Seq(
  "ohnosequences" %% "fastarious" % "0.6.0",
  "ohnosequences" %% "blast-api"  % "0.7.0",
  "ohnosequences" %% "statika"    % "2.0.0-M5",
  "ohnosequences-bundles" %% "blast"     % "0.3.0",
  "com.github.tototoshi"  %% "scala-csv" % "1.2.2",
  // Test only:
  "era7"          %% "defaults"  % "0.1.0" % Test,
  "org.scalatest" %% "scalatest" % "2.2.5" % Test
)

// shows time for each test:
testOptions in Test += Tests.Argument("-oD")
// disables parallel exec
parallelExecution in Test := false


// // Uncomment this to publish fat-jar with bundles:
// fatArtifactSettings
//
enablePlugins(BuildInfoPlugin)
buildInfoPackage := "generated.metadata.db"
buildInfoObject  := "rnacentral"
// buildInfoOptions := Seq(BuildInfoOption.Traits("ohnosequences.statika.AnyArtifactMetadata"))
buildInfoKeys    := Seq[BuildInfoKey](
  organization,
  version,
  "artifact" -> name.value
  // "artifactUrl" -> fatArtifactUrl.value
)
