package assist.controllers

import java.io.File
import java.net.URI
import javax.inject.{Inject, Singleton}

import models.{DirInfo, FilePath, PathInfo}
import org.apache.commons.io.FileUtils
import play.api.libs.circe.Circe
import play.api.libs.ws.WSClient
import play.api.mvc.InjectedController
import utils.{FileUtil, HentaiConfig}
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._

import scala.concurrent.Future

@Singleton
class FilesList @Inject() (
    commonAssets: controllers.Assets,
    hentaiConfig: HentaiConfig,
    wSClient: WSClient,
    fileUtil: FileUtil
) extends InjectedController with Circe {

  implicit def ec = defaultExecutionContext

  val rootPath = hentaiConfig.rootPath

  def at = Action.async { implicit request =>
    PathInfo.pathInfoForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest("错误的参数"))
      }, { case PathInfo(file1) =>
        println("正确的参数")
        val path = rootPath
        val parentFile = new File(path)
        val parentUrl = parentFile.toURI.toString
        val currentUrl = new URI(parentUrl + file1)
        val fileModel = new File(currentUrl)
        if (!fileModel.exists) {
          Future successful NotFound("找不到目录")
        } else if (fileModel.isDirectory) {
            val fileUrlsF = fileModel.listFiles().toList.filter(_.getName != hentaiConfig.tempDirectoryName).map { s =>
              val fileUrlString = s.toURI.toString.drop(parentUrl.size)

              val canConvert = fileUtil.canEncode(s, hentaiConfig.encodeSuffix)

              val (tempFile, temExists) = fileUtil.tempFileExists(s, hentaiConfig.tempDirectoryName)

              val tempDateFile = new File(tempFile.getParentFile, s.getName + "." + hentaiConfig.encodeInfoSuffix)
              val isEncodingF = if (tempDateFile.exists()) {
                try {
                  val dateStrings = FileUtils.readLines(tempDateFile, "utf-8")
                  val uuid = dateStrings.get(0)
                  val encodeDate = hentaiConfig.dateFormat.parse(dateStrings.get(1))
                  wSClient.url(hentaiConfig.isEncodingrUrl).withQueryStringParameters("uuid" -> uuid).get().map { wsResult =>
                    println(wsResult)

                    val resultModel = if (wsResult.status == 200) {
                      java.lang.Boolean.valueOf(wsResult.body): Boolean
                    } else {
                      false
                    }
                    resultModel
                  }
                } catch {
                  case e: Exception =>
                    e.printStackTrace
                    tempDateFile.deleteOnExit()
                    Future successful false
                }
              } else {
                Future successful false
              }

              isEncodingF.map { isEncoding =>
                FilePath(
                  fileName = s.getName,
                  requestUrl = assist.controllers.routes.Assets.at(fileUrlString).toString,
                  tempUrl = assist.controllers.routes.Assets.tempFile(fileUrlString).toString,
                  encodeUrl = s.toURI.toString.drop(parentUrl.size),
                  temfileExists = temExists,
                  canEncode = canConvert,
                  isEncoding = isEncoding
                )
              }
            }
            val periPath = fileModel.getParentFile.toURI.toString
            val preiRealPath = if (periPath.startsWith(parentFile.toURI.toString) && periPath != parentUrl) {
              val result = periPath.drop(parentUrl.size)
              assist.controllers.routes.Assets.at(result)
            } else {
              assist.controllers.routes.Assets.root
            }

            /*val currentPath = fileModel.toURI.toString
            val deleteTempUrl = if (currentPath.startsWith(parentFile.toURI.toString) && currentPath != parentUrl) {
              val result = currentPath.drop(parentUrl.size)
              assist.controllers.routes.Assets.deleteTempDir(result)
            } else {
              assist.controllers.routes.Assets.deleteRootTempDir
            }*/

            Future.sequence(fileUrlsF).map { fileUrls =>
              Ok(DirInfo(preiRealPath.toString, fileUrls).asJson)
          }
        } else {
            Future successful BadRequest("参数错误，请求的路径不是目录")
        }
      }
    )
  }

}