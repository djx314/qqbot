package assist.controllers

import java.io.File
import java.net.URI
import javax.inject.{ Inject, Named, Singleton }

import models.PathInfo
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

  def deleteTempDir = Action.async { implicit request =>
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

          val currentPath = fileModel.toURI.toString

          if (!fileModel.exists) {
            Future successful Ok("目录本身不存在")
          } else if (fileModel.isDirectory) {
            val temFile = new File(fileModel, hentaiConfig.tempDirectoryName)
            FileUtils.deleteDirectory(temFile)
            Future successful Ok("删除成功")
          } else {
            Future successful Ok("缓存目录不是文件夹，不作处理")
          }
      }
    )
  }

  def withAss(file1: String) = Action.async { implicit request =>
    val path = rootPath
    val rootFile = new File(path)
    val rootUrl = rootFile.toURI.toString
    val currentUrl = new URI(rootUrl + file1)
    val fileModel = new File(currentUrl)
    val parentFile = fileModel.getParentFile

    val fileUrl = fileModel.toURI.toString.drop(rootUrl.size)
    val parentUrl = parentFile.toURI.toString.drop(rootUrl.size)

    Future successful Ok(views.html.assEncode(fileUrl)(parentUrl))
  }

}