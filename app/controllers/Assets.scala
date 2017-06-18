package assist.controllers

import java.io.File
import java.net.URI
import java.util.Date
import javax.inject.{Inject, Named, Singleton}

import models.FileInfo
import org.apache.commons.io.FileUtils
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.{FileUtil, HentaiConfig}

import scala.concurrent.Future

@Singleton
class Assets @Inject() (
  @Named("hentai") assets: controllers.AssetsBuilder,
  commonAssets: controllers.Assets,
  components: ControllerComponents,
  hentaiConfig: HentaiConfig,
  fileUtil: FileUtil
) extends AbstractController(components) {

  implicit val ec = defaultExecutionContext

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
        val fileUrls = fileModel.listFiles().toList.filter(_.getName != hentaiConfig.tempDirectoryName).map { s =>
          val fileUrlString = s.toURI.toString.drop(parentUrl.size)

          val canConvert = fileUtil.canEncode(s, hentaiConfig.encodeSuffix)

          val (tempFile, temExists) = fileUtil.tempFileExists(s, hentaiConfig.tempDirectoryName)
          val tempString = tempFile.toURI.toString.drop(parentUrl.size)

          val tempDateFile = new File(tempFile.getParentFile, s.getName + ".EncodeDate")
          val isEncoding = if (tempDateFile.exists()) {
            val dateString = FileUtils.readFileToString(tempDateFile, "utf-8")
            val encodeDate = hentaiConfig.dateFormat.parse(dateString)
            if ((new Date().getTime - encodeDate.getTime) > (20 * 60 * 1000)) {
              false
            } else
              true
          } else {
            false
          }

          FileInfo(
            fileName = s.getName,
            requestUrl = assist.controllers.routes.Assets.at(fileUrlString),
            tempUrl = assist.controllers.routes.Assets.at(tempString),
            encodeUrl = assist.controllers.routes.Encoder.encodeFile(fileUrlString),
            temfileExists = temExists,
            canEncode = canConvert,
            isEncoding = isEncoding
          )
        }
        val periPath = fileModel.getParentFile.toURI.toString
        val preiRealPath = if (periPath.startsWith(parentFile.toURI.toString) && periPath != parentUrl) {
          val result = periPath.drop(parentUrl.size)
          assist.controllers.routes.Assets.at(result)
        } else {
          assist.controllers.routes.Assets.root
        }

        Future successful Ok(views.html.index(preiRealPath)(fileUrls))
      }
    } else {
      assets.at(path, file1)
    }
  }

  def root = at("")

  def staticAt(root: String, path: String) = commonAssets.at(root, path)

}