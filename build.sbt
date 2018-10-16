import sbt._
import sbt.Keys._

lazy val playVersion = play.core.PlayVersion.current

transitiveClassifiers in ThisBuild := Seq("sources", "jar", "javadoc")

scalacOptions ++= Seq("-encoding", "UTF-8", "-feature", "-deprecation", "-Ywarn-unused-import")

name := "qqbot"
version := "0.0.1"

scalaVersion := "2.12.7"
fork in run := false
enablePlugins(play.sbt.PlayScala, PlayAkkaHttpServer)
disablePlugins(PlayNettyServer)
addCommandAlias("drun", "run 9394")

scalafmtOnCompile := true

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
