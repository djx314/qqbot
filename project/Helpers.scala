/*package org.xarcher.sbt

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.util.Date

import scala.concurrent.ExecutionContext.Implicits.global
import sbt._
import sbt.Keys._

import scala.concurrent.Future

object Helpers {

  implicit class implicitProjectToPlay(project: Project) {

    def toPlay(filePath: String): Project = {

      (project in file(filePath))
        .enablePlugins(play.sbt.PlayScala)

    }

  }

}*/