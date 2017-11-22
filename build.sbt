name          := "api.rnacentral"
organization  := "ohnosequences"
description   := "RNACentral data API"

bucketSuffix  := "era7.com"

crossScalaVersions  := Seq("2.11.11", "2.12.4")
scalaVersion        := crossScalaVersions.value.max

libraryDependencies ++= Seq(
  "com.github.tototoshi"  %%  "scala-csv"   % "1.3.5",
  "ohnosequences"         %%  "fastarious"  % "0.12.0"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)

// wartremoverErrors in (Test, compile) := Seq()
// wartremoverErrors in (Compile, compile) := Seq()

// shows time for each test:
testOptions in Test += Tests.Argument("-oD")
// disables parallel exec
parallelExecution in Test := false