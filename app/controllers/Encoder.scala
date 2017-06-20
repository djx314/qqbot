package assist.controllers

import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.{Inject, Named, Singleton}

import models.{FileInfo, RequestInfo, VideoInfo}
import org.apache.commons.io.FileUtils
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.{EncoderInfoSend, FileUtil, HentaiConfig}

import scala.concurrent.Future
import io.circe.syntax._
import io.circe.generic.auto._
import play.api.libs.circe.Circe

@Singleton
class Encoder @Inject() (
  @Named("hentai") assets: controllers.AssetsBuilder,
  components: ControllerComponents,
  hentaiConfig: HentaiConfig,
  fileUtil: FileUtil,
  encoderInfoSend: EncoderInfoSend
) extends AbstractController(components) with Circe {

  implicit val ec = defaultExecutionContext

  val rootPath = hentaiConfig.rootPath


  def encodeFile(file1: String) = {
    val path = rootPath
    val parentFile = new File(path)
    val parentUrl = parentFile.toURI.toString
    val currentUrl = new URI(parentUrl + file1)
    val fileModel = new File(currentUrl)
    if (!fileModel.exists) {
      Action.async { implicit request =>
        Future successful NotFound("找不到目录")
      }
    } else if (fileModel.isDirectory) {
      Action.async { implicit request =>
        Future successful BadRequest("目录文件不能转码")
      }
    } else {
      Action.async { implicit request =>
        val tempDir = new File(fileModel.getParentFile, hentaiConfig.tempDirectoryName)
        tempDir.mkdirs()
        encoderInfoSend.uploadVideo(file1).map { encodeUUID =>
          val tempDateFile = new File(tempDir, fileModel.getName + "." + hentaiConfig.encodeInfoSuffix)
          val format = hentaiConfig.dateFormat
          val dateString = format.format(new Date())
          val writeString = s"$encodeUUID\r\n$dateString"
          FileUtils.writeStringToFile(tempDateFile, writeString, "utf-8")
        }
        val referUrl = request.headers.get("Referer").getOrElse(assist.controllers.routes.Assets.root().toString)
        Future successful Redirect(assist.controllers.routes.Encoder.encodeSuccessPage(referUrl))
      }
    }
  }

  def encodeSuccessPage(rederUrl: String) = Action {
    Ok(views.html.EncodeSended(rederUrl))
  }

  def uploadEncodedFile = Action.async(parse.multipartFormData(10000000000L)) { implicit request =>
    def saveTargetVideo(videoInfo: VideoInfo) = {
      val fileStr = videoInfo.videoInfo

      val path = rootPath
      val parentFile = new File(path)
      val parentUrl = parentFile.toURI.toString
      val currentUrl = new URI(parentUrl + fileStr)
      val fileModel = new File(currentUrl)

      val tempDir = new File(fileModel.getParentFile, hentaiConfig.tempDirectoryName)
      tempDir.mkdirs()

      val tempFile = new File(tempDir, fileModel.getName + ".mp4")

      request.body.file("video_0").map(s => s.ref.moveTo(tempFile, true))
      println("target:" + tempFile.getCanonicalPath)
      Ok(RequestInfo(true, tempFile.getCanonicalPath).asJson)
    }

    VideoInfo.videoForm.bindFromRequest.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        println("错误的参数：" + request.queryString.map(s => s"key:${s._1},value:${s._2}").mkString("\n"))
        println(formWithErrors)
        Future.successful(BadRequest("错误的参数"))
      },
      videoInfo => {
        println("正确的参数")
        Future successful saveTargetVideo(videoInfo)
      }
    )
  }

}