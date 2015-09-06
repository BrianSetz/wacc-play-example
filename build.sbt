name := "play-book-test"

version := "1.0-SNAPSHOT"

lazy val `play-book-test` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.

// Uncomment for Play >2.4 style (currently not supported in IntelliJ
// routesGenerator := InjectedRoutesGenerator
