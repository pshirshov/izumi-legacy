package org.bitbucket.pshirshov.izumitk.app.entrypoints

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.app.Starter
import org.bitbucket.pshirshov.izumitk.app.model.{AppArguments, EntryPoint, StartupConfiguration}
import org.bitbucket.pshirshov.izumitk.config.{LoadedConfig, ResolvedConfig}



abstract class EntrypointLoader
  extends Starter
    with StrictLogging {

  protected def appId: String

  protected def defaultArguments(): AppArguments = AppArguments()

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
          val epName = arguments.value[String](EntrypointLoader.EP_KEY)
          epMap.get(epName) match {
            case Some(e) =>
              e.run(arguments, config)
            case None =>
              throw new IllegalArgumentException(s"Unknown entry point: $epName")
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

object EntrypointLoader {
  final val EP_KEY = "izumi.entrypoint"
}
