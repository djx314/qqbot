package models

case class FileInfo(
  fileName: String,
  requestUrl: play.api.mvc.Call,
  tempUrl: play.api.mvc.Call,
  encodeUrl: play.api.mvc.Call,
  temfileExists: Boolean,
  canEncode: Boolean,
  isEncoding: Boolean
)

case class RequestInfo(isSuccessed: Boolean, message: String)

case class VideoInfo(encodeType: String, videoKey: String, videoLength: Int, videoInfo: String, returnPath: String)

object VideoInfo {

  import play.api.data._
  import play.api.data.Forms._

  val videoForm = Form(
    mapping(
      "encodeType" -> nonEmptyText,
      "videoKey" -> nonEmptyText,
      "videoLength" -> number,
      "videoInfo" -> nonEmptyText,
      "returnPath" -> nonEmptyText
    )(VideoInfo.apply)(VideoInfo.unapply)
  )
}