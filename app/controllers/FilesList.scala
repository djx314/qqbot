package assist.controllers

import java.io.File
import java.net.URLDecoder
import java.nio.file.{Files, Path, Paths}
import java.util.stream.Collectors

import archer.controllers.CommonController
import io.circe.generic.JsonCodec
import models.{DirInfo, FilePath, PathInfo, TempFileInfo}
import play.api.libs.circe.Circe
import play.api.libs.ws.WSClient
import play.api.mvc.ControllerComponents
import utils.{FileUtil, HentaiConfig}
import io.circe.syntax._
import net.scalax.asuna.sample.dto2.FutureDtoHelper
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.util.Try

class FilesList(hentaiConfig: HentaiConfig, wSClient: WSClient, fileUtil: FileUtil, controllerComponents: ControllerComponents)
    extends CommonController(controllerComponents)
    with Circe
    with FutureDtoHelper {

  val logger = LoggerFactory.getLogger(getClass)

  implicit def ec = defaultExecutionContext

  import hentaiConfig._

  def fetchIfEncoding(tempDateFile: Path) = {
    TempFileInfo
      .fromUnknowPath(tempDateFile)
      .encodeUUID
      .map { uuid =>
        wSClient
          .url(hentaiConfig.isEncodingrUrl)
          .withQueryStringParameters("uuid" -> uuid)
          .get()
          .map { wsResult =>
            if (wsResult.status == 200) {
              Try {
                java.lang.Boolean.valueOf(wsResult.body): Boolean
              }.getOrElse(false)
            } else {
              false
            }
          }
          .recover {
            case e: Exception =>
              logger.error("向后端请求是否正在转码信息错误", e)
              false
          }
      }
      .getOrElse(Future.successful(false))
  }

  def at = Action.async { implicit request =>
    PathInfo.pathInfoForm.bindFromRequest.fold(
        formWithErrors => {
        Future.successful(BadRequest("错误的参数"))
      }
      , {
        case PathInfo(file1) =>
          val path       = rootPath
          val parentFile = Paths.get(path)
          val parentUrl  = parentFile.toRealPath().toUri.toASCIIString
          //val currentUrl = new URI(parentUrl + file1)
          val currentPath = parentFile.resolve(file1).toRealPath()
          //val fileModel = new File(currentUrl)
          if (!Files.exists(currentPath)) {
            Future successful NotFound("找不到目录")
          } else if (Files.isDirectory(currentPath)) {
            val paths = Files.list(currentPath).collect(Collectors.toList()).asScala.toList
            val fileUrlsF =
              paths.filter(_.getFileName.toString != hentaiConfig.tempDirectoryName).map {
                s =>
                  object TempFileInfoTable {
                    val fileName                  = s.getFileName.toString
                    val isDirectory               = Files.isDirectory(s)
                    val requestUrl                = s.toRealPath().toUri.toASCIIString.drop(parentUrl.size)
                    val (tempFile, temfileExists) = fileUtil.tempFileExists(s, hentaiConfig.tempDirectoryName, hentaiConfig.encodeInfoSuffix)
                    val canEncode                 = fileUtil.canEncode(s.getFileName.toUri.toString, hentaiConfig.encodeSuffix)
                    val tempDateFile              = tempFile.getParent.resolve(s.getFileName.toString + "." + hentaiConfig.encodeInfoSuffix)
                    val isEncoding                = fetchIfEncoding(tempDateFile)
                  }

                  val modelf = dtoF.effect(dtoF.singleModel[FilePath](TempFileInfoTable).compile)
                  modelf.model

                /*isEncodingF.map {
                    isEncoding =>
                      FilePath(
                          fileName = s.getFileName.toString
                        , isDirectory = Files.isDirectory(s)
                        , requestUrl = s.toRealPath().toUri.toASCIIString.drop(parentUrl.size),
                        //assetsUrl = assist.controllers.routes.Assets.at(s.toRealPath().toUri.toASCIIString.drop(parentUrl.size)).toString,
                        //assetsUrl = "assets/" + s.toRealPath().toUri.toASCIIString.drop(parentUrl.size),
                        //tempUrl = assist.controllers.routes.Assets.tempFile(s.toRealPath().toUri.toASCIIString.drop(parentUrl.size)).toString,
                        //tempUrl = "tempfile/" + s.toRealPath().toUri.toASCIIString.drop(parentUrl.size),
                        //assist.controllers.routes.Assets.player(fileUrlString).toString,
                        //encodeUrl = s.toURI.toString.drop(parentUrl.size),
                        //encodeUrl = s.toRealPath().toUri.toASCIIString.drop(parentUrl.size),
                        temfileExists = temExists
                        , canEncode = canConvert
                        , isEncoding = isEncoding
                      )
                  }*/
              }
            val periPath = currentPath.getParent.toRealPath().toUri.toString
            val preiRealPath = if (periPath.startsWith(parentUrl)) {
              //val result = periPath.drop(parentUrl.size)
              periPath.drop(parentUrl.size)
            } else {
              //assist.controllers.routes.Assets.root
              ""
            }

            object TempDirInfo {
              val parentPath = preiRealPath.toString
              val urls       = Future.sequence(fileUrlsF)
            }

            val dirModelf = dtoF.effect(dtoF.singleModel[DirInfo](TempDirInfo).compile).model

            dirModelf.map { dirModel =>
              Ok(dirModel.asJson)
            }
          } else {
            Future successful BadRequest("参数错误，请求的路径不是目录")
          }
      }
    )
  }

  @JsonCodec
  case class FileSimpleInfo(fileName: String, encodeUrl: String, isDir: Boolean)

  @JsonCodec
  case class DirSimpleInfo(parentPath: String, urls: List[FileSimpleInfo])

  def atAss = Action.async { implicit request =>
    PathInfo.pathInfoForm.bindFromRequest.fold(
        formWithErrors => {
        Future.successful(BadRequest("错误的参数"))
      }
      , {
        case PathInfo(file) =>
          val file1      = URLDecoder.decode(file, "utf-8")
          val path       = rootPath
          val parentFile = new File(path)
          val parentUrl  = parentFile.toURI.toString
          val fileModel  = new File(parentFile, file1)
          if (!fileModel.exists) {
            Future successful NotFound("找不到目录")
          } else if (fileModel.isDirectory) {
            val fileUrls = fileModel.listFiles().toList.filter(_.getName != hentaiConfig.tempDirectoryName).map { s =>
              //val fileUrlString = s.toURI.toString.drop(parentUrl.size)
              FileSimpleInfo(fileName = s.getName, encodeUrl = s.toURI.toString.drop(parentUrl.size), isDir = s.isDirectory)
            }
            val periPath = fileModel.getParentFile.toURI.toString
            val preiRealPath = if (periPath.startsWith(parentFile.toURI.toString) && periPath != parentUrl) {
              val result = periPath.drop(parentUrl.size)
              result
            } else {
              ""
            }

            Future successful Ok(DirSimpleInfo(preiRealPath, fileUrls).asJson)
          } else {
            Future successful BadRequest("参数错误，请求的路径不是目录")
          }
      }
    )
  }

}
