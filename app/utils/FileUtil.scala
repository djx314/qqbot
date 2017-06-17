package utils

import java.io.File
import javax.inject.{Inject, Singleton}

trait FileUtil {

  def tempFileExists(file: File, tempDirectoryName: String): (File, Boolean) = {
    val tempDirectory = new File(file.getParentFile, tempDirectoryName)
    tempDirectory.mkdirs()
    val tempFile = new File(tempDirectory, file.getName + ".mp4")
    tempFile -> tempFile.exists()
  }

  def canEncode(file: File, suffix: Seq[String]): Boolean = {
    if (file.getName.lastIndexOf('.') >= 0) {
      val fileSuffix = file.getName.takeRight(file.getName.size - file.getName.lastIndexOf('.') - 1)
      println(fileSuffix)
      suffix.exists(_ == fileSuffix)
    } else {
      false
    }
  }

}

@Singleton
class FileUtilImpl @Inject() (
                                 ) extends FileUtil