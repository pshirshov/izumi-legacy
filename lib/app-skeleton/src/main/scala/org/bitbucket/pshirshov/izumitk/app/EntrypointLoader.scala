package org.bitbucket.pshirshov.izumitk.app

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.app.model.AppModel.{StartupConfiguration, WithBaseArguments}
import org.bitbucket.pshirshov.izumitk.app.model.{EPArguments, EntryPoint}
import org.bitbucket.pshirshov.izumitk.config.{LoadedConfig, ResolvedConfig}
import org.bitbucket.pshirshov.izumitk.modularity.PluginsSupport

class BootstrapPluginsLoader
(
  override protected val basePackage: Package
  , override protected val appId: String
  , override protected val config: LoadedConfig
)
  extends PluginsSupport {
  override protected def namespace: String = "entrypoints"
}

abstract class EntrypointLoader[T <: WithBaseArguments with EPArguments]
  extends Starter[T]
    with StrictLogging {

  protected def appId: String

  protected def defaultArguments(): T

  protected val bootstrapReference: Config = ConfigFactory.load("izumi-entrypoint-bootstrap.conf")
  // TODO: looks shitty
  protected val bootstrapConfig: LoadedConfig = ResolvedConfig(bootstrapReference, bootstrapReference, bootstrapReference)

  protected val bootstrap: BootstrapPluginsLoader = new BootstrapPluginsLoader(getClass.getPackage, appId, bootstrapConfig)

  def main(args: Array[String]): Unit = {
    val epMap = loadEntrypoints()
    epMap.values.foreach(_.configure(parser))
    configuration(args, defaultArguments()) match {
      case StartupConfiguration(arguments, config) =>
        safeMain {
          epMap.get(arguments.entrypoint) match {
            case Some(e) =>
              e.run(arguments, config)
            case None =>
              throw new IllegalArgumentException(s"Unknown entry point: ${arguments.entrypoint}")
          }
        }
    }
  }

  protected def loadEntrypoints(): Map[String, EntryPoint] = {
    val epMap: Map[String, EntryPoint] = {
      val entrypoints = bootstrap.loadPlugins().filter(_.isInstanceOf[EntryPoint]).map(_.asInstanceOf[EntryPoint])
      logger.info(s"Entrypoints loaded: ${entrypoints.map(_.name)}")

      entrypoints.groupBy(_.name).map {
        case (k, v) =>
          k -> v.head
      }
    }
    epMap
  }
}
