

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "V4S4J",
    idePackagePrefix := Some("V4S4J.V4S4J")
  )

libraryDependencies ++= Seq(
  "dev.capslock" % "voicevoxcore4s_2.13" % "0.14.1-ALPHA-3",
  "org.scala-lang" % "scala-library" % "2.13.13"
)