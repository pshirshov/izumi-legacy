package org.bitbucket.pshirshov.izumitk.config

import java.io.File
import java.util.concurrent.atomic.AtomicReference

import com.typesafe.config.{Config, ConfigFactory, ConfigLoadingStrategy, ConfigParseOptions}
import com.typesafe.scalalogging.StrictLogging


class NamedConfigLoadingStrategy extends ConfigLoadingStrategy with StrictLogging {
  override def parseApplicationConfig(parseOptions: ConfigParseOptions): Config = {
    val overrideOptions: ConfigParseOptions = parseOptions.setAllowMissing(false)
    val path = NamedConfigLoadingStrategy.configPath.get()

    try {
      ConfigFactory.parseFile(new File(path), overrideOptions)
    } catch {
      case t: Throwable =>
        logger.error(s"Failed to load application config at `$path`, trying as resource: `$t`")
        logger.debug(s"Config failure@`$path`:", t)
        ConfigFactory.parseResources(parseOptions.getClassLoader, path, overrideOptions)
    }
  }
}

object NamedConfigLoadingStrategy {
  final val configPath = new AtomicReference[String]()

  def init(path: String) = {
    System.setProperty("config.strategy", classOf[NamedConfigLoadingStrategy].getCanonicalName)
    configPath.set(path)
  }
}

class FailingConfigLoadingStrategy extends ConfigLoadingStrategy {
  override def parseApplicationConfig(parseOptions: ConfigParseOptions): Config = {
    throw new IllegalStateException(s"Default config loading is prohibited!")
  }
}

object FailingConfigLoadingStrategy {
  def init() = {
    System.setProperty("config.strategy", classOf[FailingConfigLoadingStrategy].getCanonicalName)
  }
}
