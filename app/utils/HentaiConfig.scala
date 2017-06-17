package utils

import javax.inject.{Inject, Singleton}

import play.api.Configuration

trait HentaiConfig {

  val rootPath: String
  val encodeSuffix: Seq[String]
  val tempDirectoryName: String

}

@Singleton
class HentaiConfigImpl @Inject() (
                                 configuration: Configuration
                                 ) extends HentaiConfig {

  override val rootPath = configuration.get[String]("djx314.hentai.root.path")
  override val encodeSuffix = configuration.get[Seq[String]]("djx314.hentai.encode.suffix")
  override val tempDirectoryName = configuration.get[String]("djx314.hentai.encode.temp.directory.name")

}