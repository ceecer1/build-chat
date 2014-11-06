name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.10" % "2.3.5",
  cache,
  ws
)
