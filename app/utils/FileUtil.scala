package utils

import java.io.File
import java.nio.file.{ Files, Path }
import javax.inject.{ Inject, Singleton }

import models.{ DateTimeFormat, TempFileInfo }

trait FileUtil {

  def tempFileExists(file: Path, tempDirectoryName: String, encodeInfoSuffix: String)(implicit format: DateTimeFormat): (Path, Boolean) = {
    val tempDirectory = file.getParent.resolve(tempDirectoryName)
    Files.createDirectories(tempDirectory)

    val tempInfoFile = tempDirectory.resolve(file.getFileName.toString + "." + encodeInfoSuffix)
    val tempInfo = TempFileInfo.fromUnknowPath(tempInfoFile)
    val tempFile = tempDirectory.resolve(file.getFileName.toString + "." + tempInfo.encodeSuffix)
    tempFile -> Files.exists(tempFile)
  }

  def canEncode(fileName: String, suffix: Seq[String]): Boolean = {
    if (fileName.lastIndexOf('.') >= 0) {
      val fileSuffix = fileName.takeRight(fileName.size - fileName.lastIndexOf('.') - 1)
      //println(fileSuffix)
      suffix.exists(_ == fileSuffix)
    } else {
      false
    }
  }

}

@Singleton
class FileUtilImpl @Inject() () extends FileUtil