import sbt._
import sbt.Keys._

lazy val playVersion = play.core.PlayVersion.current

transitiveClassifiers in ThisBuild := Seq("sources", "jar", "javadoc")

scalacOptions ++= Seq("-encoding", "UTF-8", "-feature", "-deprecation", "-Ywarn-unused-import")

name := "hentaidesu"
version := "0.0.1"

val circeVersion = "0.10.0"

libraryDependencies += ws
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.2"
libraryDependencies += "commons-io"        % "commons-io"              % "2.5"
libraryDependencies += "com.dripower"      %% "play-circe"             % "2610.0"
libraryDependencies += "io.circe"          %% "circe-optics"           % circeVersion
libraryDependencies += "io.circe"          %% "circe-generic-extras"   % circeVersion
libraryDependencies += "net.scalax"        %% "asuna-mapper"           % "0.0.2-SNAP20181003.1"
//libraryDependencies += "org.apache.httpcomponents" % "httpmime" % "4.5.3"

libraryDependencies ++= Seq(
    "com.softwaremill.macwire" %% "macros"     % "2.3.0" % "provided"
  , "com.softwaremill.macwire" %% "macrosakka" % "2.3.0" % "provided"
  , "com.softwaremill.macwire" %% "util"       % "2.3.0"
  , "com.softwaremill.macwire" %% "proxy"      % "2.3.0"
)

libraryDependencies ++= Seq(
    "org.webjars.bower" % "requirejs"      % "2.3.3"
  , "org.webjars.bower" % "requirejs-text" % "2.0.15"
)

scalaVersion := "2.12.6"
fork in run := false
enablePlugins(play.sbt.PlayScala, PlayAkkaHttpServer)
disablePlugins(PlayNettyServer)
addCommandAlias("drun", "run 9527")

dependsOn(hentaiBase)
lazy val hentaiBase = (project in file("./hentai-base"))

scalafmtOnCompile := true

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
