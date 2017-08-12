package utils

import java.io.File
import javax.inject.{Inject, Singleton}

import models.{DateTimeFormat, TempFileInfo}

trait FileUtil {

  def tempFileExists(file: File, tempDirectoryName: String, encodeInfoSuffix: String)(implicit format: DateTimeFormat): (File, Boolean) = {
    val tempDirectory = new File(file.getParentFile, tempDirectoryName)
    tempDirectory.mkdirs()

    val tempInfoFile = new File(tempDirectory, file.getName + "." + encodeInfoSuffix)
    val tempInfo = TempFileInfo.fromUnknowPath(tempInfoFile.toPath)
    val tempFile = new File(tempDirectory, file.getName + "." + tempInfo.encodeSuffix)
    tempFile -> tempFile.exists()
  }

  def canEncode(file: File, suffix: Seq[String]): Boolean = {
    if (file.getName.lastIndexOf('.') >= 0) {
      val fileSuffix = file.getName.takeRight(file.getName.size - file.getName.lastIndexOf('.') - 1)
      //println(fileSuffix)
      suffix.exists(_ == fileSuffix)
    } else {
      false
    }
  }

}

@Singleton
class FileUtilImpl @Inject() () extends FileUtil