package assist.controllers

import java.io.File
import java.net.URI
import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents}

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
    val parentFile = new File(path)
    val parentUrl = parentFile.toURI.toString
    val currentUrl = new URI(parentUrl + file1)
    val fileModel = new File(currentUrl)
    if (! fileModel.exists) {
      Action.async { implicit request =>
        Future successful NotFound("找不到目录")
      }
    } else if (fileModel.isDirectory) {
      Action.async { implicit request =>
        val fileUrls = fileModel.listFiles().toList.map { s =>
          val fileUrlString = s.toURI.toString.drop(parentUrl.size)
          assist.controllers.routes.Assets.at(fileUrlString) -> s.getName
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
  def baidu(file1: String) = assets.at("E:/pro/workspace/baiduMap/baidumapTest", file1)

}