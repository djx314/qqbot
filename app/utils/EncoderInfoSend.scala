package utils

import java.nio.file.{ Files, Path, Paths }
import javax.inject.{ Inject, Singleton }

import akka.stream.scaladsl.{ FileIO, Source }
import org.slf4j.LoggerFactory
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.{ DataPart, FilePart }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure

@Singleton
class EncoderInfoSend @Inject() (
    ws: WSClient,
    hentaiConfig: HentaiConfig
)(implicit defaultExecutionContext: scala.concurrent.ExecutionContext) {

  val logger = LoggerFactory.getLogger(classOf[EncoderInfoSend])
  //implicit val _ec = defaultExecutionContext
  def uploadVideo(fileStr: Path): Future[String] = Future {

    val path = Paths.get(hentaiConfig.rootPath)
    //val parentFile = Paths.get(path, fileStr)
    val sourceFile = fileStr
    //val parentUrl = parentFile.toURI.toString
    //val currentUrl = new URI(parentUrl + fileStr)
    //val sourceFile = new File(currentUrl)
    val key = s"里番-${sourceFile.getFileName}"

    val fileExists = Files.exists(sourceFile)

    logger.info(
      s"""开始发送里番文件
         |文件名:${sourceFile.getFileName}
         |文件是否存在于文件系统:${if (fileExists) "是" else "否"}""".stripMargin
    )

    ws.url(hentaiConfig.encoderUrl)
      .withRequestTimeout(1.hour)
      .post(
      Source(
        FilePart("video_0", sourceFile.getFileName.toString, Option("text/plain"), FileIO.fromPath(sourceFile)) ::
          DataPart("videoKey", key) ::
          DataPart("videoInfo", sourceFile.toUri.toString.drop(path.toUri.toString.size)) ::
          DataPart("returnPath", hentaiConfig.selfUrl) ::
          //DataPart("encodeType", "FormatFactoryEncoder") ::
          DataPart("encodeType", "ffmpegEncoder") ::
          DataPart("videoLength", 1.toString) ::
          Nil
      )
    )
      .map { wsResult =>
        val resultModel = if (wsResult.status == 200) {
          //RequestInfo(true, wsResult.body)
          logger.info(
            s"""上传文件成功
               |文件名:${sourceFile.getFileName}
               |文件路径:${sourceFile}
             """.stripMargin
          )
          wsResult.body
        } else {
          val errorStr =
            s"""上传文件返回异常代码:${wsResult.status}
               |文件路径:${sourceFile}
               |错误内容:\n${wsResult.body}""".stripMargin
          //RequestInfo(false, errorStr)
          logger.error(errorStr)
          wsResult.body
        }
        resultModel
      }.andThen {
        case Failure(e) =>
          logger.error(s"""上传文件失败
                          |文件名:${sourceFile.getFileName}
                          |文件路径:${sourceFile}""".stripMargin, e)
      }
  }.flatMap(identity)

  def uploadVideoWithAss(videoFile: Path, assFile: Path): Future[String] = Future {
    val path = hentaiConfig.rootPath
    val parentFile = Paths.get(path)
    val parentUrl = parentFile.toUri.toString
    val videoPath = videoFile
    val assPath = assFile
    val videoPathExists = Files.exists(videoPath)
    val assPathExists = Files.exists(assPath)

    val key = s"里番-${videoPath.getFileName}"

    logger.info(
      s"""开始发送里番文件
         |视频文件名:${videoPath.getFileName}
         |视频文件是否存在于文件系统:${if (videoPathExists) "是" else "否"}
         |字幕文件名:${assPath.getFileName}
         |字幕文件是否存在于文件系统:${if (assPathExists) "是" else "否"}""".stripMargin
    )

    ws.url(hentaiConfig.encoderUrl)
      .withRequestTimeout(1.hour)
      .post(
      Source(
        FilePart("video_0", videoPath.getFileName.toString, Option("text/plain"), FileIO.fromPath(videoPath)) ::
          FilePart("video_1", assPath.getFileName.toString, Option("text/plain"), FileIO.fromPath(assPath)) ::
          DataPart("videoKey", key) ::
          DataPart("videoInfo", videoPath.toUri.toString.drop(parentUrl.size)) ::
          DataPart("returnPath", hentaiConfig.selfUrl) ::
          //DataPart("encodeType", "FormatFactoryEncoder") ::
          DataPart("encodeType", "ffmpegEncoderWithAss") ::
          DataPart("videoLength", 2.toString) ::
          Nil
      )
    )
      .map { wsResult =>
        val resultModel = if (wsResult.status == 200) {
          //RequestInfo(true, wsResult.body)
          logger.info(
            s"""上传文件成功
               |视频文件名:${videoPath.getFileName}
               |字幕文件名:${assPath.getFileName}
               |视频文件路径:${videoPath}
               |字幕文件路径:${assPath}
             """.stripMargin
          )
          wsResult.body
        } else {
          val errorStr =
            s"""上传文件返回异常代码:${wsResult.status}
               |视频文件名:${videoPath.getFileName}
               |字幕文件名:${assPath.getFileName}
               |视频文件路径:${videoPath}
               |字幕文件路径:${assPath}
               |错误内容:\n${wsResult.body}""".stripMargin
          //RequestInfo(false, errorStr)
          logger.error(errorStr)
          wsResult.body
        }
        resultModel
      }.andThen {
        case Failure(e) =>
          logger.error(s"""上传文件失败
                        |文件名:${videoPath.getFileName}
                        |${assPath.getFileName}
                        |文件路径:${videoPath}
                        |${assPath}""".stripMargin, e)
      }
  }.flatMap(identity)

}