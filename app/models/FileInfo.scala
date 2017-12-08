package models

case class FilePath(
  fileName: String,
  isDirectory: Boolean,
  requestUrl: String,
  assetsUrl: String,
  tempUrl: String,
  encodeUrl: String,
  temfileExists: Boolean,
  canEncode: Boolean,
  isEncoding: Boolean
)

case class DirInfo(parentPath: String, urls: List[FilePath])

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

case class PathInfo(path: String)

object PathInfo {
  import play.api.data._
  import play.api.data.Forms._

  val pathInfoForm = Form(
    mapping(
      "path" -> text
    )(PathInfo.apply)(PathInfo.unapply)
  )
}

case class AssPathInfo(videoPath: String, assPath: String, assScale: BigDecimal)

object AssPathInfo {
  import play.api.data._
  import play.api.data.Forms._

  val assPathInfoForm = Form(
    mapping(
      "videoPath" -> nonEmptyText,
      "assPath" -> nonEmptyText,
      "assScale" -> bigDecimal
    )(AssPathInfo.apply)(AssPathInfo.unapply)
  )
}