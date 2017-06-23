package utils

import java.io.File
import java.net.URI
import javax.inject.{ Inject, Singleton }

import akka.stream.scaladsl.{ FileIO, Source }
import models.RequestInfo
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.{ DataPart, FilePart }

import scala.concurrent.Future

@Singleton
class EncoderInfoSend @Inject() (
    ws: WSClient,
    hentaiConfig: HentaiConfig,
    defaultExecutionContext: scala.concurrent.ExecutionContext
) {

  implicit val _ec = defaultExecutionContext

  def uploadVideo(fileStr: String): Future[String] = Future {

    val path = hentaiConfig.rootPath
    val parentFile = new File(path)
    val parentUrl = parentFile.toURI.toString
    val currentUrl = new URI(parentUrl + fileStr)
    val sourceFile = new File(currentUrl)

    val key = s"里番-${sourceFile.getName}"

    println(s"发送里番文件:${sourceFile.getCanonicalPath},文件是否存在:${sourceFile.exists()}")

    ws.url(hentaiConfig.encoderUrl).post(
      Source(
        FilePart("video_0", sourceFile.getName, Option("text/plain"), FileIO.fromPath(sourceFile.toPath)) ::
          DataPart("videoKey", key) ::
          DataPart("videoInfo", fileStr) ::
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
          wsResult.body
        } else {
          RequestInfo(false, s"请求失败，错误码${wsResult.body}")
          wsResult.body
        }
        println(resultModel)
        resultModel
      }
  }.flatMap(identity)

  def uploadVideoWithAss(videoFile: File, assFile: File): Future[String] = Future {

    val path = hentaiConfig.rootPath
    val parentFile = new File(path)
    val parentUrl = parentFile.toURI.toString

    val key = s"里番-${videoFile.getName}"

    println(s"发送里番文件:${videoFile.getCanonicalPath},文件是否存在:${videoFile.exists()}")
    println(s"发送里番字幕文件:${assFile.getCanonicalPath},文件是否存在:${assFile.exists()}")

    ws.url(hentaiConfig.encoderUrl).post(
      Source(
        FilePart("video_0", videoFile.getName, Option("text/plain"), FileIO.fromPath(videoFile.toPath)) ::
          FilePart("video_1", assFile.getName, Option("text/plain"), FileIO.fromPath(assFile.toPath)) ::
          DataPart("videoKey", key) ::
          DataPart("videoInfo", videoFile.toURI.toString.drop(parentUrl.size)) ::
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
          wsResult.body
        } else {
          RequestInfo(false, s"请求失败，错误码${wsResult.body}")
          wsResult.body
        }
        println(resultModel)
        resultModel
      }
  }.flatMap(identity)

}