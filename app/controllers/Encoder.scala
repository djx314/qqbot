package assist.controllers

import java.io.File
import java.net.{URI, URLDecoder}
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util.Date
import javax.inject.{Inject, Named, Singleton}

import archer.controllers.CommonController
import controllers.CustomAssets
import models._
import org.apache.commons.io.FileUtils
import play.api.mvc.{ControllerComponents, InjectedController}
import utils.{EncoderInfoSend, FileUtil, HentaiConfig}

import scala.concurrent.Future
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.optics.JsonPath._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.circe.Circe
import play.utils.UriEncoding

import scala.collection.JavaConverters._

@Singleton
class Encoder @Inject() (
    @Named("hentai") assets: CustomAssets,
    hentaiConfig: HentaiConfig,
    fileUtil: FileUtil,
    encoderInfoSend: EncoderInfoSend,
    controllerComponents: ControllerComponents
) extends CommonController(controllerComponents) with Circe {

  import hentaiConfig._

  val logger = LoggerFactory.getLogger(getClass)

  implicit def ec = defaultExecutionContext

  val rootPath = hentaiConfig.rootPath

  def encodeFile = Action.async { implicit request =>
    PathInfo.pathInfoForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest("错误的参数"))
      }, {
        case PathInfo(file1) =>
          val path = rootPath
          val parentFile = new File(path)
          val parentUrl = parentFile.toURI.toString
          val currentUrl = new URI(parentUrl + file1)
          val fileModel = new File(currentUrl)
          if (!fileModel.exists) {
            Future successful NotFound("找不到目录")
          } else if (fileModel.isDirectory) {
            Future successful BadRequest("目录文件不能转码")
          } else {
            val tempDir = new File(fileModel.getParentFile, hentaiConfig.tempDirectoryName)
            tempDir.mkdirs()
            encoderInfoSend.uploadVideo(Paths.get(currentUrl)).map { encodeUUID =>
              val tempInfo = TempFileInfo(
                encodeUUID = Option(encodeUUID),
                encodeTime = Option(DateTime.now),
                encodeSuffix = "mp4"
              )
              val tempDateFile = new File(tempDir, fileModel.getName + "." + hentaiConfig.encodeInfoSuffix)
              Files.write(tempDateFile.toPath, List(tempInfo.beautifulJson).asJava, Charset.forName("utf-8"))
            }
            //val referUrl = request.headers.get("Referer").getOrElse(assist.controllers.routes.Assets.root().toString)
            Future successful Ok("转码指令发送成功，喵")
          }
      }
    )
  }

  def saveAssInfo = Action.async { implicit request =>
    AssPathInfo.assPathInfoForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest("错误的参数"))
      }, {
        case AssPathInfo(videoFilePath, assFilePath, assScale) =>
          val path = rootPath
          val parentFile = new File(path)
          //val parentUrl = parentFile.toURI.toString
          val currentUrl = new File(parentFile, URLDecoder.decode(videoFilePath, "utf-8")).toURI
          val videoFile = new File(currentUrl)
          //val assUrl = new URI(parentUrl + assFilePath)
          //val assFile = new File(assUrl)

          val tempDir = new File(videoFile.getParentFile, hentaiConfig.tempDirectoryName)
          val tempInfoFile = new File(tempDir, videoFile.getName + "." + hentaiConfig.encodeInfoSuffix)

          val tempInfo = TempFileInfo.fromUnknowPath(tempInfoFile.toPath)
          val newTempInfo = tempInfo.copy(assFilePath = Option(assFilePath), assScale = assScale)
          Files.write(tempInfoFile.toPath, List(newTempInfo.beautifulJson).asJava, Charset.forName("utf-8"))

          Future successful Ok("存储字幕信息成功，喵")
          /*if ((!videoFile.exists) || (!assFile.exists)) {
            Future successful NotFound("找不到目录")
          } else if (videoFile.isDirectory || assFile.isDirectory) {
            Future successful BadRequest("目录文件不能转码")
          } else {
            val tempDir = new File(videoFile.getParentFile, hentaiConfig.tempDirectoryName)
            tempDir.mkdirs()
            encoderInfoSend.uploadVideoWithAss(videoFile.toPath, assFile.toPath).map { encodeUUID =>
              val tempDateFile = new File(tempDir, videoFile.getName + "." + hentaiConfig.encodeInfoSuffix)
              val format = hentaiConfig.dateFormat
              val dateString = "2333"//format.format(new Date())
              val writeString = s"$encodeUUID\r\n$dateString"
              FileUtils.writeStringToFile(tempDateFile, writeString, "utf-8")
            }
            Future successful Ok("带字幕转码指令发送成功，喵")
          }*/
      }
    )
  }

  def uploadEncodedFile = Action.async(parse.multipartFormData(Long.MaxValue)) { implicit request =>
    def saveTargetVideo(videoInfo: VideoInfo) = {
      val fileStr = videoInfo.videoInfo

      val path = rootPath
      val parentFile = new File(path)
      val parentUrl = parentFile.toURI.toString
      val currentUrl = new URI(parentUrl + fileStr)
      val fileModel = new File(currentUrl)

      val tempDir = new File(fileModel.getParentFile, hentaiConfig.tempDirectoryName)
      tempDir.mkdirs()

      val tempInfoFile = new File(tempDir, fileModel.getName + "." + hentaiConfig.encodeInfoSuffix)

      val tempInfo = TempFileInfo.fromUnknowPath(tempInfoFile.toPath)

      val tempFile = new File(tempDir, fileModel.getName + "." + tempInfo.encodeSuffix)

      request.body.file("video_0").map(s => s.ref.moveTo(tempFile, true))
      logger.info("服务器返回转码后的文件,目标路径为:\n" + tempFile.getCanonicalPath)
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
        //println("正确的参数")
        Future successful saveTargetVideo(videoInfo)
      }
    )
  }

}