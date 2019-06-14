libraryDependencies ++= Seq(
  "ohnosequences"        %% "s3"         % "0.2.1",
  "ohnosequences"        %% "files"      % "0.5.0",
  "com.github.tototoshi" %% "scala-csv"  % "1.3.5",
  "ohnosequences"        %% "fastarious" % "0.12.0",
  "ohnosequences"        %% "faster"     % "0.2.0",
  "ohnosequences"        %% "std"        % "0.0.0-28-gf2b625e",
) ++ testDependencies

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

dependencyOverrides ++= Seq(
  "ohnosequences" %% "bits" % "0.2.2"
)
