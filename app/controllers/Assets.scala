package assist.controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}
import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}
import play.utils.UriEncoding

import scala.concurrent.Future

@Singleton
class Assets @Inject() (assets: controllers.CustomAssets,
                        components: ControllerComponents,
                        configure: Configuration) extends AbstractController(components) {

  implicit val ec = defaultExecutionContext

  val rootPath = {
    configure.get[String]("djx314.path")
  }
  def at(file1: String) = {
    val path = rootPath
    val file = UriEncoding.decodePathSegment(file1, "utf-8")

    val parentFile = new File(path)
    val fileModel = new File(parentFile, file)
    if (! fileModel.exists) {
      Action.async { implicit request =>
        Future successful NotFound("找不到目录")
      }
    } else if (fileModel.isDirectory) {
      Action.async { implicit request =>
        val fileUrls = fileModel.listFiles().toList.map { s =>
          val fileUrlString = s.getCanonicalPath.drop(new File(rootPath).getCanonicalPath.size)
          assist.controllers.routes.Assets.at(UriEncoding.encodePathSegment(fileUrlString, "utf-8")) -> s.getName
        }
        val periPath = fileModel.getParentFile.getCanonicalPath
        val preiRealPath = if (periPath.startsWith(parentFile.getCanonicalPath) && periPath != parentFile.getCanonicalPath) {
          val result = periPath.drop(new File(rootPath).getCanonicalPath.size)
          assist.controllers.routes.Assets.at(UriEncoding.encodePathSegment(result, "utf-8"))
        } else {
          assist.controllers.routes.Assets.root
        }

        Future successful Ok(views.html.index(preiRealPath)(fileUrls))
      }
    } else {
      assets.at(path, UriEncoding.encodePathSegment(fileModel.getCanonicalPath.drop(parentFile.getCanonicalPath.size), "utf-8"))
    }
  }

  def root = at("")

}