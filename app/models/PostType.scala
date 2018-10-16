package qqbot.models

import io.circe.Decoder
import io.circe.generic.JsonCodec

import scala.util.Success

@JsonCodec
case class SimpleInfo(post_type: String, message_type: String)

@JsonCodec
case class Sender(nickname: String, sex: String, age: Int)

@JsonCodec
case class SiLiao(
    post_type: String
  , message_type: String
  , sub_type: String
  , message_id: Int
  , user_id: Int
  , message: String
  , raw_message: String
  , font: Int
  , sender: Sender
) extends PostType

@JsonCodec
case class QunXiaoXi(
    post_type: String
  , message_type: String
  , sub_type: String
  , message_id: Int
  , group_id: Int
  , user_id: Int
  , message: String
  , raw_message: String
  , font: Int
  , sender: Sender
) extends PostType

sealed trait PostType

object PostType {
  implicit val decoder: Decoder[PostType] = {
    Decoder.decodeJson.emapTry { json =>
      //println(json.noSpaces)
      json.as[SimpleInfo].toTry.flatMap { info =>
        if (info.post_type == "message") {
          if (info.message_type == "private") {
            json.as[SiLiao].toTry
          } else if (info.message_type == "group") {
            json.as[QunXiaoXi].toTry

          } else Success(new UnExceptType {})
        } else Success(new UnExceptType {})
      }
    }
  }
}

trait UnExceptType extends PostType

sealed trait SendMessageContect {
  def message_type: String
  def message: String
  def auto_escape: Boolean

}

@JsonCodec
case class SendUserMessage(
    override val message_type: String = "private"
  , user_id: Int
  , override val message: String
  , override val auto_escape: Boolean = false
) extends SendMessageContect

@JsonCodec
case class SendGroupMessage(
    override val message_type: String = "group"
  , group_id: Int
  , override val message: String
  , override val auto_escape: Boolean = false
) extends SendMessageContect

@JsonCodec
case class SendDiscussMessage(
    override val message_type: String = "discuss"
  , discuss_id: Int
  , override val message: String
  , override val auto_escape: Boolean = false
) extends SendMessageContect
