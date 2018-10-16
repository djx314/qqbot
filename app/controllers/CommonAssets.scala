package archer.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.ControllerComponents
import qqbot.models.ImageTable
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.H2Profile.api._

import scala.util.Random

@Singleton
class CommonAssetsController @Inject()(commonAssets: controllers.Assets, controllerComponents: ControllerComponents, dbConfig: DatabaseConfig[JdbcProfile])
    extends CommonController(controllerComponents) {

  implicit def ec = defaultExecutionContext

  def staticAt(root: String, path: String) = commonAssets.at(root, path)

  def createTable = Action.async { implicit request =>
    dbConfig.db.run(ImageTable.schema.create).map { _: Unit =>
      Ok("建表成功")
    }
  }

  def cleanTable = Action.async { implicit request =>
    dbConfig.db.run(ImageTable.delete).map { rows: Int =>
      Ok(s"删除了${rows}行数据")
    }
  }

  def random = Action.async { implicit request =>
    for {
      size <- dbConfig.db.run(ImageTable.size.result)
      num = Random.nextInt(size)
      id    <- dbConfig.db.run(ImageTable.sortBy(_.id.desc).map(_.id).drop(num).result.head)
      image <- dbConfig.db.run(ImageTable.filter(_.id === id).result.head)
    } yield {
      Ok(image.imageContent)
    }

  }

}
