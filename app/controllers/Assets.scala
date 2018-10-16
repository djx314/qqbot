package assist.controllers

import java.net.URL
import java.nio.file.{Files, Paths}
import java.util.UUID

import akka.util.ByteString
import archer.controllers._
import io.circe.{Json, Printer}
import io.circe.syntax._
import org.apache.commons.io.IOUtils
import play.api.libs.ws.{BodyWritable, InMemoryBody, WSClient, WSResponse}
import play.api.mvc.ControllerComponents
import org.slf4j.LoggerFactory
import qqbot.models._
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.Try

class Assets(
    commonAssets: controllers.Assets
  , wSClient: WSClient
  , dbConfig: DatabaseConfig[JdbcProfile]
  , controllerComponents: ControllerComponents
) extends CommonController(controllerComponents) {

  implicit def bodyWritableOf_Json(implicit printer: Printer = Printer.noSpaces): BodyWritable[Json] = {
    BodyWritable(json => InMemoryBody(ByteString.fromString(json.pretty(printer))), "application/json")
  }

  implicit def ec = defaultExecutionContext

  val logger = LoggerFactory.getLogger(getClass)

  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index()))
  }

  def sendMessage(content: SendMessageContect): Future[WSResponse] = {
    val url = "http://127.0.0.1:5700/send_msg"
    content match {
      case userMsg: SendUserMessage =>
        wSClient.url(url).post(userMsg.asJson)
      case groupMsg: SendGroupMessage =>
        wSClient.url(url).post(groupMsg.asJson)
      case discussMsg: SendDiscussMessage =>
        wSClient.url(url).post(discussMsg.asJson)
    }
  }

  val imageDir      = "custom_image"
  val imageRootPath = Paths.get("F:").resolve("CQP-xiaoi").resolve("酷Q Pro").resolve("data").resolve("image")
  val imageDirPath  = imageRootPath.resolve(imageDir)

  def getPicFromUrl(url: URL): String = {
    val uuid = UUID.randomUUID.toString
    Files.createDirectories(imageDirPath)
    Try {
      val conn = url.openConnection
      conn.connect
      val ins = conn.getInputStream
      try {
        Files.copy(ins, imageDirPath.resolve(uuid))
      } finally {
        try {
          ins.close
        } catch {
          case _: Throwable =>
        }
      }
    }
    uuid
  }

  def getPicByteFromUrl(cqNumber: String): Array[Byte] = {
    val conn = new URL(cqNumber)
    IOUtils.toByteArray(conn)
  }

  val imagePrefix = "[CQ:image,file="
  val urlPrefix   = ",url="

  val atPrefix = "[CQ:at,qq="

  def qqbotEvent = Action.async(circe.json[PostType]) { implicit request =>
    logger.info(request.body.toString)
    request.body match {
      case siLiao: SiLiao if siLiao.message.indexOf("批量录入") >= 0 && siLiao.message.indexOf(imagePrefix) >= 0 && siLiao.user_id == 909134790 =>
        @tailrec
        def getUrls(str: String, contents: List[Array[Byte]]): List[Array[Byte]] = {
          val index = str.indexOf(urlPrefix)
          if (index >= 0) {
            val fileUrl = str.drop(index + urlPrefix.size)
            val left    = fileUrl.dropWhile(s => s != ',')
            val url     = fileUrl.takeWhile(s => s != ',')
            println(left)
            getUrls(left, getPicByteFromUrl(url) :: contents)
          } else
            contents
        }

        val pics = getUrls(siLiao.message, List.empty)

        dbConfig.db
          .run(ImageTable ++= pics.map(arr => Image(id = -1, imageContent = arr)))
          .flatMap { row: Option[Int] =>
            row match {
              case Some(r) =>
                sendMessage(SendUserMessage(user_id = siLiao.user_id, message = s"录入${siLiao.sender.nickname}的${r}张图片成功"))
              case _ =>
                sendMessage(SendUserMessage(user_id = siLiao.user_id, message = s"录入图片失败"))
            }
          }
          .recover {
            case e: Exception => e.printStackTrace
          }

      case siLiao: SiLiao if siLiao.message.indexOf("录入") >= 0 && siLiao.message.indexOf(imagePrefix) >= 0 && siLiao.user_id == 909134790 =>
        val fileUrl = siLiao.message.drop(siLiao.message.indexOf(urlPrefix) + urlPrefix.size).takeWhile(s => s != ',')
        val arr     = getPicByteFromUrl(fileUrl)
        dbConfig.db.run(ImageTable += Image(id = -1, imageContent = arr)).flatMap { row: Int =>
          sendMessage(SendUserMessage(user_id = siLiao.user_id, message = s"${siLiao.sender.nickname}的图片录入成功"))
        }

      /*val fileUrl = siLiao.message.drop(siLiao.message.indexOf(urlPrefix) + urlPrefix.size).takeWhile(s => s != ',')
        val arr     = getPicByteFromUrl(fileUrl)
        dbConfig.db.run(ImageTable += Image(id = -1, imageContent = arr)).flatMap { row: Int =>
          sendMessage(SendUserMessage(user_id = siLiao.user_id, message = s"${siLiao.sender.nickname}的图片录入成功"))
        }*/
      case siLiao: SiLiao if siLiao.message.trim == "语录" =>
        sendMessage(
            SendUserMessage(
              user_id = siLiao.user_id
            , message = s"""[CQ:image,file=${imageDir}/${getPicFromUrl(new URL("http://127.0.0.1:9394/random"))}]"""
          )
        )

      case qunXiaoXi: QunXiaoXi if qunXiaoXi.message.trim == "谁是傻逼" =>
        if (qunXiaoXi.user_id == 909134790) {
          sendMessage(SendGroupMessage(group_id = qunXiaoXi.group_id, message = "我是傻逼"))
        } else {
          sendMessage(SendGroupMessage(group_id = qunXiaoXi.group_id, message = "你才是傻逼"))
        }

      case qunXiaoXi: QunXiaoXi if qunXiaoXi.message.trim == "抱抱我" =>
        sendMessage(SendGroupMessage(group_id = qunXiaoXi.group_id, message = s"抱抱${qunXiaoXi.sender.nickname}"))

      case qunXiaoXi: QunXiaoXi if (qunXiaoXi.message.trim == "语录") && (qunXiaoXi.group_id == 174651452) =>
        sendMessage(
            SendGroupMessage(
              group_id = qunXiaoXi.group_id
            , message = s"""[CQ:image,file=${imageDir}/${getPicFromUrl(new URL("http://127.0.0.1:9394/random"))}]"""
          )
        )

      case qunXiaoXi: QunXiaoXi if qunXiaoXi.message.drop(qunXiaoXi.message.indexOf(atPrefix) + atPrefix.size).takeWhile(s => s != ']') == "1296471773" =>
        sendMessage(
            SendGroupMessage(
              group_id = qunXiaoXi.group_id
            , message = s"""喵,艾特我干嘛?[CQ:at,qq=${qunXiaoXi.user_id}]"""
          )
        )
      /*case qunXiaoXi: QunXiaoXi if qunXiaoXi.message.indexOf("录入") >= 0 && qunXiaoXi.message.indexOf(imagePrefix) >= 0 =>
        val fileUrl = qunXiaoXi.message.drop(qunXiaoXi.message.indexOf(urlPrefix) + urlPrefix.size).takeWhile(s => s != ',')
        val arr     = getPicByteFromUrl(fileUrl)
        dbConfig.db.run(ImageTable += Image(id = -1, imageContent = arr)).flatMap { row: Int =>
          sendMessage(SendGroupMessage(group_id = qunXiaoXi.group_id, message = s"${qunXiaoXi.sender.nickname}的图片录入成功"))
        }*/

      case _ =>
    }
    Future.successful(Ok(views.html.index()))
  }

}
