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
libraryDependencies += "play-circe" %% "play-circe" % "2.6-0.8.0"

scalaVersion := "2.12.2"
fork in run := false
enablePlugins(play.sbt.PlayScala, PlayAkkaHttpServer)
disablePlugins(PlayNettyServer)

dependsOn(hentaiBase)
lazy val hentaiBase = (project in file("./hentai-base"))