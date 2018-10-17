package archer.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.ControllerComponents
import qqbot.models.{ImageTable, NiubiImageTable}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.H2Profile.api._

import scala.util.Random

import java.awt.{Color, Font, Toolkit}
import java.awt.datatransfer.{DataFlavor, Transferable, UnsupportedFlavorException}
import java.io._
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageInputStream

import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.filters.{Canvas, Caption}
import net.coobird.thumbnailator.geometry.Positions

@Singleton
class CommonAssetsController @Inject()(commonAssets: controllers.Assets, controllerComponents: ControllerComponents, dbConfig: DatabaseConfig[JdbcProfile])
    extends CommonController(controllerComponents) {

  implicit def ec = defaultExecutionContext

  def staticAt(root: String, path: String) = commonAssets.at(root, path)

  def createTable = Action.async { implicit request =>
    dbConfig.db.run(NiubiImageTable.schema.create).map { _: Unit =>
      Ok("建表成功")
    }
  }

  def cleanTable = Action.async { implicit request =>
    dbConfig.db.run((ImageTable.delete >> NiubiImageTable.delete).transactionally).map { rows: Int =>
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

  def niubiRandom(message: String) = Action.async { implicit request =>
    for {
      size <- dbConfig.db.run(NiubiImageTable.size.result)
      num = Random.nextInt(size)
      id    <- dbConfig.db.run(NiubiImageTable.sortBy(_.id.desc).map(_.id).drop(num).result.head)
      image <- dbConfig.db.run(NiubiImageTable.filter(_.id === id).result.head)
      newImage = pic(image.imageContent)(message)
    } yield {
      Ok(newImage)
    }
  }

  def strLength(content: String): Int = {
    /*content.toList.map { s =>
      if (s.hashCode > 128) 2 else 1
    }.sum*/
    (content.getBytes("UTF-8").length + content.length) / 4
  }

  def pic(file: Array[Byte])(content: String): Array[Byte] = {

    val inputStream: InputStream      = new ByteArrayInputStream(file)
    val formatNameStrean: InputStream = new ByteArrayInputStream(file)
    val outStream                     = new ByteArrayOutputStream()
    try {
      val formatName = ImageIO.getImageReaders(ImageIO.createImageInputStream(formatNameStrean)).next().getFormatName

      val aaa          = ImageIO.read(inputStream)
      val targetWidth  = aaa.getWidth
      val fontSize     = Math.max(16, (targetWidth * 0.7 / strLength(content))).toInt
      val targetHeight = aaa.getHeight
      val bbb          = Thumbnails.of(aaa).size(targetWidth, targetHeight)

      // Set up the caption properties
      val caption     = content
      val font        = new Font("Microsoft YaHei", Font.PLAIN, fontSize)
      val c           = Color.black
      val position    = Positions.BOTTOM_CENTER
      val insetPixels = (fontSize * 0.48).toInt

      // Apply caption to the image
      val filter = new Caption(caption, font, c, position, insetPixels)
      val colorFilter = new Canvas(
          Math.max(fontSize * (content.getBytes("UTF-8").length + content.length) / 4 + 10, targetWidth)
        , targetHeight + (fontSize * 1.48).toInt
        , Positions.TOP_CENTER
        , Color.WHITE
      )

      val captionedImage = bbb.addFilter(colorFilter).addFilter(filter).asBufferedImage()
      ImageIO.write(captionedImage, formatName, outStream)
      outStream.toByteArray()

      /*val captionedImage   = bbb.addFilter(colorFilter).addFilter(filter).asBufferedImage()
      val clipborad        = Clipboard.systemClipboard
      val clipboardContent = new ClipboardContent()
      clipboardContent.putImage {
        ImageIO.write(captionedImage, formatName, outStream)
        val is = new ByteArrayInputStream(outStream.toByteArray())
        new Image(is)
      }
      clipborad.content = clipboardContent*/
    } catch {
      case e: Throwable =>
        e.printStackTrace
        throw e
    } finally {
      try {
        inputStream.close
      } catch {
        case e: Exception =>
          e.printStackTrace
      }
      try {
        outStream.close
      } catch {
        case e: Exception =>
          e.printStackTrace
      }
      try {
        formatNameStrean.close
      } catch {
        case e: Exception =>
          e.printStackTrace
      }
    }

  }

}
