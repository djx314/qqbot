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

//libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.2"
libraryDependencies += "commons-io" % "commons-io" % "2.5"
libraryDependencies += "com.dripower" % "play-circe_2.12" % "2609M2.0"
libraryDependencies += "io.circe" %% "circe-optics" % "0.8.0"
libraryDependencies += "io.circe" %% "circe-generic-extras" % "0.8.0"
//libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.3"
//libraryDependencies += "org.apache.httpcomponents" % "httpmime" % "4.5.3"

libraryDependencies ++= Seq(
  "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided",
  "com.softwaremill.macwire" %% "macrosakka" % "2.3.0" % "provided",
  "com.softwaremill.macwire" %% "util" % "2.3.0",
  "com.softwaremill.macwire" %% "proxy" % "2.3.0"
)

scalaVersion := "2.12.4"
fork in run := false
enablePlugins(play.sbt.PlayScala, PlayAkkaHttpServer)
disablePlugins(PlayNettyServer)
addCommandAlias("drun", "run 9527")

dependsOn(hentaiBase)
lazy val hentaiBase = (project in file("./hentai-base"))

