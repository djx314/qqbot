package qqbot.models

import slick.jdbc.H2Profile.api._

case class Image(id: Int, imageContent: Array[Byte])
class ImageTable(tag: Tag) extends Table[Image](tag, "image") {
  def id           = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def imageContent = column[Array[Byte]]("image_content")

  override def * = (id, imageContent).<>(Image.tupled, Image.unapply)
}
object ImageTable extends TableQuery(cons => new ImageTable(cons))
