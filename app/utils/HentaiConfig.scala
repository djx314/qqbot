package utils

import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}

import play.api.Configuration

trait HentaiConfig {

  val rootPath: String
  val encodeSuffix: Seq[String]
  val tempDirectoryName: String
  val encoderUrl: String
  val selfUrl: String

  def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

}

@Singleton
class HentaiConfigImpl @Inject() (
                                 configuration: Configuration
                                 ) extends HentaiConfig {

  override val rootPath = configuration.get[String]("djx314.hentai.root.path")
  override val encodeSuffix = configuration.get[Seq[String]]("djx314.hentai.encode.suffix")
  override val tempDirectoryName = configuration.get[String]("djx314.hentai.encode.temp.directory.name")

  override val encoderUrl = configuration.get[String]("djx314.hentai.url.encoder")
  override val selfUrl = configuration.get[String]("djx314.hentai.url.self")

}