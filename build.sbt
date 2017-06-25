import sbt._
import sbt.Keys._

val printlnDo = println("""
courage
""".stripMargin
)

lazy val playVersion = play.core.PlayVersion.current

transitiveClassifiers in ThisBuild := Seq("sources", "jar", "javadoc")

name := "hentaidesu"
version := "0.0.1"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "commons-io" % "commons-io" % "2.5"

scalaVersion := "2.11.11"
fork in run := false
enablePlugins(play.sbt.PlayScala, PlayAkkaHttpServer)
disablePlugins(PlayNettyServer)

dependsOn(playCirce)

lazy val playCirce = (project in file("./play-circe"))
.settings(scalaVersion := "2.11.11")