package assist.controllers

import java.io.File
import java.net.URI
import javax.inject.{ Inject, Named, Singleton }

import models.FileInfo
import org.apache.commons.io.FileUtils
import play.api.libs.ws.WSClient
import play.api.mvc.InjectedController
import play.utils.UriEncoding
import utils.{ FileUtil, HentaiConfig }

import scala.concurrent.Future

@Singleton
class Assets @Inject() (
    @Named("hentai") assets: controllers.AssetsBuilder,
    commonAssets: controllers.Assets,
    hentaiConfig: HentaiConfig,
    wSClient: WSClient,
    fileUtil: FileUtil
) extends InjectedController {

  implicit def ec = defaultExecutionContext

  val rootPath = hentaiConfig.rootPath

  def at(file1: String) = {
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
        val fileUrlsF = fileModel.listFiles().toList.filter(_.getName != hentaiConfig.tempDirectoryName).map { s =>
          val fileUrlString = s.toURI.toString.drop(parentUrl.size)

          val canConvert = fileUtil.canEncode(s, hentaiConfig.encodeSuffix)

          val (tempFile, temExists) = fileUtil.tempFileExists(s, hentaiConfig.tempDirectoryName)
          //val tempString = tempFile.toURI.toString.drop(parentUrl.size)

          val tempDateFile = new File(tempFile.getParentFile, s.getName + "." + hentaiConfig.encodeInfoSuffix)
          val isEncodingF = if (tempDateFile.exists()) {
            try {
              val dateStrings = FileUtils.readLines(tempDateFile, "utf-8")
              val uuid = dateStrings.get(0)
              val encodeDate = hentaiConfig.dateFormat.parse(dateStrings.get(1))
              println(uuid)
              wSClient.url(hentaiConfig.isEncodingrUrl).withQueryStringParameters("uuid" -> uuid).get().map { wsResult =>
                println(wsResult)

                val resultModel = if (wsResult.status == 200) {
                  java.lang.Boolean.valueOf(wsResult.body): Boolean
                } else {
                  false
                }
                println(resultModel)
                resultModel
              }
              /*if ((new Date().getTime - encodeDate.getTime) > (20 * 60 * 1000)) {
                false
              } else
                true*/
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
            FileInfo(
              fileName = s.getName,
              requestUrl = assist.controllers.routes.Assets.at(fileUrlString),
              tempUrl = assist.controllers.routes.Assets.tempFile(fileUrlString),
              encodeUrl = assist.controllers.routes.Encoder.encodeFile(fileUrlString),
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

        val currentPath = fileModel.toURI.toString
        val deleteTempUrl = if (currentPath.startsWith(parentFile.toURI.toString) && currentPath != parentUrl) {
          val result = currentPath.drop(parentUrl.size)
          assist.controllers.routes.Assets.deleteTempDir(result)
        } else {
          assist.controllers.routes.Assets.deleteRootTempDir
        }

        Future.sequence(fileUrlsF).map { fileUrls =>
          Ok(views.html.index(preiRealPath)(deleteTempUrl)(fileUrls))
        }
      }
    } else {
      assets.at(path, file1)
    }
  }

  def root = at("")

  def staticAt(root: String, path: String) = commonAssets.at(root, path)

  def tempFile(file1: String) = {
    val path = rootPath
    val parentFile = new File(path)
    val parentUrl = parentFile.toURI.toString
    val currentUrl = new URI(parentUrl + file1)
    val fileModel = new File(currentUrl)
    if (!fileModel.exists) {
      Action.async { implicit request =>
        Future successful NotFound("找不到文件")
      }
    } else if (fileModel.isDirectory) {
      Action.async { implicit request =>
        Future successful NotFound("找不到文件")
      }
    } else {
      val tempDir = new File(fileModel.getParentFile, hentaiConfig.tempDirectoryName)
      val tempFile = new File(tempDir, fileModel.getName + ".mp4")
      if (!tempFile.exists) {
        Action.async { implicit request =>
          Future successful NotFound("缓存文件不存在")
        }
      } else {
        val path = rootPath
        val parentFile = new File(path)
        val parentUrl = parentFile.getCanonicalPath
        val tempString = tempFile.getCanonicalPath.drop(parentUrl.size)

        //val tempFinalString = tempString.replaceAllLiterally(File.separator, "/")

        println(tempString)
        println(tempFile.getCanonicalPath)
        //println(tempFinalString)
        assets.at(path, UriEncoding.encodePathSegment(tempString, "utf-8"))
        //assets.at(path, tempFinalString)
      }
    }
  }

  def deleteTempDir(file1: String) = {
    val path = rootPath
    val parentFile = new File(path)
    val parentUrl = parentFile.toURI.toString
    val currentUrl = new URI(parentUrl + file1)
    val fileModel = new File(currentUrl)

    val currentPath = fileModel.toURI.toString
    val redirectUrl = if (currentPath.startsWith(parentFile.toURI.toString) && currentPath != parentUrl) {
      val result = currentPath.drop(parentUrl.size)
      assist.controllers.routes.Assets.at(file1)
    } else {
      assist.controllers.routes.Assets.root
    }

    Action.async { implicit request =>
      if (!fileModel.exists) {
        Future successful Redirect(redirectUrl)
      } else if (fileModel.isDirectory) {
        val temFile = new File(fileModel, hentaiConfig.tempDirectoryName)
        FileUtils.deleteDirectory(temFile)
        Future successful Redirect(redirectUrl)
      } else {
        Future successful Redirect(redirectUrl)
      }
    }

  }

  def deleteRootTempDir = deleteTempDir("")

}