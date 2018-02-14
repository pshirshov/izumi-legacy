package org.bitbucket.pshirshov.izumitk.app.entrypoints

import java.util.concurrent.atomic.AtomicReference

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.app.Starter
import org.bitbucket.pshirshov.izumitk.app.model.{AppArguments, EntryPoint, StartupConfiguration}
import org.bitbucket.pshirshov.izumitk.config.{LoadedConfig, ResolvedConfig}
import org.bitbucket.pshirshov.izumitk.modularity.PluginsSupport

abstract class EntrypointLoader
  extends Starter
    with StrictLogging {

  protected def defaultArguments(): AppArguments = AppArguments()

  protected def bootstrapReference: Config = ConfigFactory.load("izumi-entrypoint-bootstrap.conf")

  // TODO: looks shitty
  protected def bootstrapConfig: LoadedConfig = ResolvedConfig(
    bootstrapReference
    , bootstrapReference
    , bootstrapReference
  )

  protected def bootstrapLoader: BootstrapPluginsLoader = BootstrapPluginsLoader(getClass.getPackage.getName, bootstrapConfig)

  protected val entrypoint = new AtomicReference[EntryPoint](null)

  def main(args: Array[String]): Unit = {
    safeMain {
      Option(entrypoint.getAndSet(null)).foreach {
        oldEp =>
          logger.warn(s"Entrypoint is expected to be null on app start but it was set to $oldEp")
      }
      
      val btMap = loadBoostrapEntrypoints()
      btMap.values.foreach(_.configure(parser))

      configuration(args, defaultArguments()) match {
        case StartupConfiguration(arguments, config) =>
          val epName = arguments.value[String](EntrypointLoader.EP_KEY)
          val epMap = loadEntrypoints(BootstrapPluginsLoader(s"${getClass.getPackage.getName}.$epName", bootstrapConfig))
          (btMap ++ epMap).get(epName) match {
            case Some(e) =>
              entrypoint.set(e)
              e.run(arguments, config)
            case None =>
              throw new IllegalArgumentException(s"Unknown entry point: $epName")
          }
      }
    }
  }

  protected def loadBoostrapEntrypoints(): Map[String, EntryPoint] = {
    loadEntrypoints(bootstrapLoader)
  }

  protected def loadEntrypoints(pluginLoader: PluginsSupport): Map[String, EntryPoint] = {
    val entrypoints = pluginLoader.loadPlugins().filter(_.isInstanceOf[EntryPoint]).map(_.asInstanceOf[EntryPoint])
    logger.info(s"Entrypoints loaded: ${entrypoints.map(_.name)}")

    entrypoints.groupBy(_.name).map {
      case (k, v) =>
        k -> v.head
    }
  }
}

object EntrypointLoader {
  final val EP_KEY = "izumi.entrypoint"
}
