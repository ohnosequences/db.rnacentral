Nice.scalaProject

name          := "18s-its-database"
organization  := "era7bio"
description   := "18s-its-database project"

bucketSuffix  := "era7.com"

libraryDependencies ++= Seq(
  "ohnosequences" %% "fastarious" % "0.3.0",
  "ohnosequences" %% "blast-api"  % "0.4.1",
  "org.scalatest" %% "scalatest" % "2.2.5" % Test
)
