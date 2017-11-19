package utils

import controllers.AssetsBuilder
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

object AssetsUtil {

  def at(builder: AssetsBuilder, path: String, file: String, aggressiveCaching: Boolean = false)(implicit request: RequestHeader): Future[Result] = {
    val c = classOf[AssetsBuilder]
    val m = c.getDeclaredMethod("assetAt", classOf[String], classOf[String], classOf[Boolean], classOf[RequestHeader])
    m.setAccessible(true)
    m.invoke(builder, path, file, false: java.lang.Boolean, request).asInstanceOf[Future[Result]]
  }

}