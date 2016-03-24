Nice.scalaProject

name          := "rnacentraldb"
organization  := "era7bio"
description   := "Code generation and bundle for a 18S RNA databases"

bucketSuffix  := "era7.com"

libraryDependencies ++= Seq(
  "ohnosequences" %% "fastarious" % "0.4.0-SNAPSHOT",
  "ohnosequences" %% "blast-api"  % "0.4.1",
  "ohnosequences" %% "statika"    % "2.0.0-M5",
  "com.github.tototoshi" %% "scala-csv" % "1.2.2",
  "era7" %% "defaults" % "0.1.0-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "2.2.5" % Test
)

// shows time for each test:
testOptions in Test += Tests.Argument("-oD")
// disables parallel exec
parallelExecution in Test := false

fatArtifactSettings

enablePlugins(BuildInfoPlugin)
buildInfoPackage := "generated.metadata"
buildInfoObject  := name.value.split("""\W""").map(_.capitalize).mkString
buildInfoOptions := Seq(BuildInfoOption.Traits("ohnosequences.statika.AnyArtifactMetadata"))
buildInfoKeys    := Seq[BuildInfoKey](
  organization,
  version,
  "artifact" -> name.value.toLowerCase,
  "artifactUrl" -> fatArtifactUrl.value
)
