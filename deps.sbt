libraryDependencies ++= Seq(
  "ohnosequences"        %% "s3"         % "0.1.0-33-g493e0c3",
  "ohnosequences"        %% "files"      % "0.4.0",
  "com.github.tototoshi" %% "scala-csv"  % "1.3.5",
  "ohnosequences"        %% "fastarious" % "0.12.0"
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
