// generic conf; don't change this file
// pull particular versions from buildconf
scalaVersion := "2.12.6"

dependencyOverrides += "org.scala-lang" % "scala-library" % scalaVersion.value

// compiler
////////////////////////////////////////////////////////////////////////////////
scalacOptions ++= Seq(
  "-Xsource:2.13",
  "-Xlint",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-adapted-args",
  "-Ywarn-unused-import",
  "-Xlog-reflective-calls",
  "-opt-warnings:_",
  "-Ydelambdafy:method"
)

// scaladoc
////////////////////////////////////////////////////////////////////////////////
scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits", "-feature")

addCompilerPlugin("ohnosequences" %% "contexts" % "0.5.0")
////////////////////////////////////////////////////////////////////////////////

// scalafmt
////////////////////////////////////////////////////////////////////////////////
scalafmtVersion := "1.4.0"
scalafmtOnCompile := true
////////////////////////////////////////////////////////////////////////////////

// wartemover
////////////////////////////////////////////////////////////////////////////////
wartremoverErrors in (Compile, compile) := Seq()

wartremoverWarnings in (Compile, compile) := Warts.allBut(
  Wart.Equals,
  Wart.FinalVal,
  Wart.ImplicitConversion,
  Wart.Nothing // needed because of the contexts compiler plugin
)

wartremoverErrors in (Test, compile) := Seq()

wartremoverWarnings in (Test, compile) := Warts.allBut(
  Wart.Equals,
  Wart.FinalVal,
  Wart.ImplicitConversion,
  Wart.Nothing // needed because of the contexts compiler plugin
)
////////////////////////////////////////////////////////////////////////////////

// Test configuration
////////////////////////////////////////////////////////////////////////////////
// shows time for each test:
testOptions in Test += Tests.Argument("-oD")
// disables parallel exec
parallelExecution in Test := false
////////////////////////////////////////////////////////////////////////////////

// publishing
////////////////////////////////////////////////////////////////////////////////
bucketSuffix := "era7.com"
awsProfile := s"${organization.value}/${name.value}"
////////////////////////////////////////////////////////////////////////////////
