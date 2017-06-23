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
        Future successful Ok(views.html.index(file1))
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