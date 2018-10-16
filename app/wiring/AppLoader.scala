package wiring

import akka.stream.Materializer
import play.api.ApplicationLoader.Context
import play.api._
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.ahc.AhcWSComponents
import router.Routes
import com.softwaremill.macwire._
import play.api.db.slick.{DbName, SlickComponents}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class InjectedAhcWSComponents(
    override val environment: Environment
  , override val configuration: Configuration
  , override val applicationLifecycle: ApplicationLifecycle
  , override val materializer: Materializer
  , override val executionContext: ExecutionContext
) extends AhcWSComponents

class InjectedSlickComponents(
    override val environment: Environment
  , override val configuration: Configuration
  , override val applicationLifecycle: ApplicationLifecycle
  , override val executionContext: ExecutionContext
) extends SlickComponents

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) with NoHttpFiltersComponents {

  private implicit def as      = actorSystem
  private implicit lazy val ec = executionContext

  /**
    * Assets 模块配置开始
    */
  private lazy val AssetsConfigurationProvider                                          = wire[_root_.controllers.AssetsConfigurationProvider]
  private def AssetsConfigurationGen(a: _root_.controllers.AssetsConfigurationProvider) = a.get
  private lazy val AssetsConfiguration                                                  = wireWith(AssetsConfigurationGen _)

  private lazy val AssetsMetadataProvider                                     = wire[_root_.controllers.AssetsMetadataProvider]
  private def AssetsMetadataGen(a: _root_.controllers.AssetsMetadataProvider) = a.get
  private lazy val AssetsMetadata                                             = wireWith(AssetsMetadataGen _)

  private lazy val Assets                 = wire[_root_.controllers.Assets]
  private lazy val archerAssets           = wire[assist.controllers.Assets]
  private lazy val CommonAssetsController = wire[archer.controllers.CommonAssetsController]

  private lazy val slickApi: SlickComponents             = wire[InjectedSlickComponents]
  private lazy val dbConfig: DatabaseConfig[JdbcProfile] = slickApi.slickApi.dbConfig(DbName("default"))

  /**
    * Assets 模块配置结束
    */
  /**
    * ws 模块配置开始
    */
  private lazy val InjectedAhcWSComponents = wire[InjectedAhcWSComponents]
  private def wsGen(a: AhcWSComponents)    = a.wsClient
  private lazy val ws                      = wireWith(wsGen _)

  // Router
  override lazy val router = {
    val routePrefix: String = "/"
    wire[Routes]
  }

}
