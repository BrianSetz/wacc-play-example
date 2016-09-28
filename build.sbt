name := """wacc-play-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DockerPlugin).settings(
  dockerExposedPorts := Seq(9000)
)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  specs2 % Test
)

fork in run := true
