package assist.controllers

import java.io.File
import java.net.URI
import java.nio.file.{ Files, Paths }
import javax.inject.{ Inject, Named, Singleton }

import archer.controllers.CommonController
import controllers.CustomAssets
import models.{ PathInfo, TempFileInfo }
import org.apache.commons.io.FileUtils
import play.api.libs.ws.WSClient
import play.api.mvc.ControllerComponents
import play.utils.UriEncoding
import utils.{ AssetsUtil, FileUtil, HentaiConfig }
import io.circe.syntax._
import io.circe._
import io.circe.generic.auto._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.Future

@Singleton
class Assets @Inject() (
  assets: CustomAssets,
  commonAssets: controllers.Assets,
  hentaiConfig: HentaiConfig,
  wSClient: WSClient,
  fileUtil: FileUtil,
  controllerComponents: ControllerComponents) extends CommonController(controllerComponents) {

  import hentaiConfig._

  implicit def ec = defaultExecutionContext

  val logger = LoggerFactory.getLogger(getClass)

  def at(file1: String) = Action.async { implicit request =>
    //val path = rootPath
    /*val parentFile = new File(path)
    val parentUrl = parentFile.toURI.toString
    val currentUrl = new URI(parentUrl + file1)
    val fileModel = new File(currentUrl)
    if (!fileModel.exists) {
        Future successful NotFound("找不到目录")
    } else if (fileModel.isDirectory) {
      Future successful Ok(views.html.index(file1))
    } else {
      AssetsUtil.at(assets, path, file1)
    }*/
    //AssetsUtil.at(assets, path, file1)
    //println(file1)
    //val url = s"http://192.168.1.112/" + UriEncoding.encodePathSegment(s"${file1}", "utf-8")
    //println(url)
    Future.successful(MovedPermanently(s"http://192.168.1.112/$file1"))
  }

  def files = Action.async { implicit request =>
    Future.successful(Ok(views.html.index()))
  }
  //def root = at("")
  def staticAt(root: String, path: String) = commonAssets.at(root, path)

  def tempFile(file1: String) = Action.async { implicit request =>
    /*val parentPath = Paths.get(rootPath)
    println(parentPath.toUri.toString)
    val currentFile = Paths.get(parentPath.toUri.resolve(file1))
    println(file1)
    println(currentFile.toRealPath())
    val urlSuffix =
      s"${currentFile.toRealPath().toUri.toASCIIString.drop(parentPath.toRealPath().toUri.toASCIIString.size)}"
    Future.successful(Redirect(s"http://192.168.1.112/$urlSuffix"))*/
    //val path = rootPath
    val parentFile = Paths.get(rootPath)
    val currentUrl = parentFile.toUri.resolve(file1)
    val fileModel = Paths.get(currentUrl)
    if (!Files.exists(fileModel)) {
      Future successful NotFound("找不到文件")
    } else if (Files.isDirectory(fileModel)) {
      Future successful NotFound("找不到文件")
    } else {
      val tempDir = fileModel.getParent.resolve(hentaiConfig.tempDirectoryName)
      val tempInfoFile = tempDir.resolve(fileModel.getFileName.toString + "." + hentaiConfig.encodeInfoSuffix)

      val tempInfo = TempFileInfo.fromUnknowPath(tempInfoFile)

      val tempFile = tempDir.resolve(fileModel.getFileName.toString + "." + tempInfo.encodeSuffix)

      if (!Files.exists(tempFile)) {
        Future successful NotFound("缓存文件不存在")
      } else {
        logger.info(s"访问转码后的文件:\n${tempFile.toRealPath().toString}")
        //val tempFinalString = tempString.replaceAllLiterally(File.separator, "/")
        //println(tempString)
        //println(tempFile.getCanonicalPath)
        //println(tempFinalString)
        //AssetsUtil.at(assets, path, UriEncoding.encodePathSegment(tempString, "utf-8"))
        //val parentPathStr = parentFile.toRealPath().toString
        //Future.successful(Redirect(s"http://192.168.1.112:91/迅雷下载/${tempFile.toPath.toRealPath().toUri.toString.drop(parentUrl.size)}"))
        //val url = s"http://192.168.1.112:91/" + UriEncoding.encodePathSegment(s"迅雷下载/${tempFile.toPath.toRealPath().toString.drop(parentPathStr.size)}", "utf-8")
        //val url = s"http://192.168.1.112:91/迅雷下载/${tempFile.toPath.toRealPath().toUri.toString.drop(parentUrl.size)}"
        val url = s"${tempFile.toRealPath().toUri.toASCIIString.drop(parentFile.toRealPath().toUri.toASCIIString.size)}"
        //UriEncoding.encodePathSegment(UriEncoding.decodePath(s"迅雷下载/${tempFile.toPath.toRealPath().toUri.toString.drop(parentUrl.size + 1)}", "utf-8"), "utf-8")
        Future.successful(MovedPermanently(s"http://192.168.1.112/$url"))
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
      })
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

  def player(file1: String) = Action.async { implicit request =>
    val path = rootPath
    val parentFile = new File(path)
    val parentUrl = parentFile.toURI.toString
    val currentUrl = new URI(parentUrl + file1)
    val fileModel = new File(currentUrl)

    val tempDir = new File(fileModel.getParentFile, hentaiConfig.tempDirectoryName)
    val tempInfoFile = new File(tempDir, fileModel.getName + "." + hentaiConfig.encodeInfoSuffix)

    val tempInfo = TempFileInfo.fromUnknowPath(tempInfoFile.toPath)

    Future successful Ok(views.html.player(file1)(tempInfo.assFilePath)(tempInfo.assScale))
  }

}