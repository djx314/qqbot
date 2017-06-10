import sbt._
import sbt.Keys._

val printlnDo = println("""
courage
""".stripMargin
)

lazy val playVersion = play.core.PlayVersion.current

transitiveClassifiers in ThisBuild := Seq("sources", "jar", "javadoc")

name := "freedom"
version := "0.0.1"
libraryDependencies += guice
scalaVersion := "2.11.11"
fork in run := false
enablePlugins(play.sbt.PlayScala)