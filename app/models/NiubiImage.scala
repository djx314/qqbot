package qqbot.models

import slick.jdbc.H2Profile.api._

case class NiubiImage(id: Int, imageContent: Array[Byte])
class NiubiImageTable(tag: Tag) extends Table[NiubiImage](tag, "niubi_image") {
  def id           = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def imageContent = column[Array[Byte]]("image_content")

  override def * = (id, imageContent).<>(NiubiImage.tupled, NiubiImage.unapply)
}
object NiubiImageTable extends TableQuery(cons => new NiubiImageTable(cons))
