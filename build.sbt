Nice.scalaProject

name          := "18s-its-database"
organization  := "era7bio"
description   := "Code generation and bundle for a 18S RNA databases"

bucketSuffix  := "era7.com"

libraryDependencies ++= Seq(
  "ohnosequences" %% "fastarious" % "0.3.0",
  "ohnosequences" %% "blast-api"  % "0.4.1",
  "ohnosequences" %% "statika"    % "2.0.0-M5",
  "org.scalatest" %% "scalatest" % "2.2.5" % Test
)
