package net.scalax.mp4.modules

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import controllers.CustomAssets
import utils.{ FileUtil, FileUtilImpl, HentaiConfig, HentaiConfigImpl }

class Mp4Module extends AbstractModule {

  def configure() = {
    bind(classOf[controllers.AssetsBuilder])
      .annotatedWith(Names.named("hentai"))
      .to(classOf[CustomAssets])

    bind(classOf[HentaiConfig])
      .to(classOf[HentaiConfigImpl])

    bind(classOf[FileUtil])
      .to(classOf[FileUtilImpl])
    /*bind(classOf[VideoPathConfig])
      .to(classOf[VideoConfig])

    bind(classOf[FilesReply])
      .to(classOf[FilesReplyImpl])*/
  }

}