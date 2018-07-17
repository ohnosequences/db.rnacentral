libraryDependencies ++= Seq(
  "ohnosequences" %% "aws-scala-tools" % "0.20.0"
) ++ testDependencies

resolvers := Seq(
  "Era7 private maven releases" at s3("private.releases.era7.com").toHttps(
    s3region.value.toString)
) ++ resolvers.value

val testDependencies = Seq(
  "era7bio"       %% "defaults"  % "0.3.0-RC3" % Test,
  "org.scalatest" %% "scalatest" % "3.0.4"     % Test,
  "ohnosequences" %% "statika"   % "3.0.0"     % Test
)

dependencyOverrides ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % Test,
  "org.scala-lang" % "scala-library"  % scalaVersion.value % Test,
  "org.scala-lang" % "scala-reflect"  % scalaVersion.value % Test,
  "org.scala-lang" % "scalap"         % scalaVersion.value
)
