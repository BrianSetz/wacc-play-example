name := """wacc-play-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DockerPlugin).settings(
  dockerExposedPorts := Seq(9000)
)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  specs2 % Test,
  "org.reactivemongo" %% "reactivemongo" % "0.11.14",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14"
)

fork in run := true
