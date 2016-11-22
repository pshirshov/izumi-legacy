package org.bitbucket.pshirshov.izumitk.config


import java.io.File

import com.typesafe.config._
import com.typesafe.scalalogging.StrictLogging

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

sealed trait LoadedConfig {
  val effective: Config
  val effectiveApp: Config
  val reference: Config
}

case class ResolvedConfig(effective: Config, effectiveApp: Config, reference: Config) extends LoadedConfig

case class LoadedPath(effective: Config, effectiveApp: Config, reference: Config) extends LoadedConfig

case class LoadedResource(effective: Config, effectiveApp: Config, reference: Config) extends LoadedConfig

object TypesafeConfigLoader extends StrictLogging {
  def loadConfig(path: String, referenceConfigName: String): Try[LoadedConfig] = {
    logger.info(s"Loading config `$path` with reference `$referenceConfigName`...")
    val options = ConfigResolveOptions.defaults()
    val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)

    val loader = classloader()
    val reference = ConfigFactory.parseResources(referenceConfigName, parseOptions)

    Try {
      val source = ConfigFactory.parseFile(new File(path), parseOptions)

      val effectiveAppConfig = ConfigFactory.defaultOverrides(loader)
        .withFallback(source)
        .resolve(options)

      val resolvedConfig = effectiveAppConfig
        .withFallback(ConfigFactory.defaultReference(loader))
        .resolve(options)

      LoadedPath(resolvedConfig, cleanupEffectiveAppConfig(effectiveAppConfig, reference), reference)
    } recoverWith {
      case NonFatal(t) =>
        if (!t.isInstanceOf[ConfigException.IO]) {
          logger.info(s"Failed to parse config at path `$path`, trying as resource...", t)
        } else {
          logger.info(s"IO error at filesystem path `$path`, trying as resource...")
        }

        Try {
          val effectiveAppConfig = ConfigFactory.defaultOverrides(loader)
            .withFallback(reference)
            .resolve(options)

          val resolvedConfig = ConfigFactory
            .load(referenceConfigName, parseOptions, options)
            .resolve(options)

          LoadedResource(resolvedConfig, cleanupEffectiveAppConfig(effectiveAppConfig, reference), reference)
        } match {
          case v@Failure(t1) =>
            v
          case v@Success(_) =>
            v
        }
    }
  }

  private final def cleanupEffectiveAppConfig(effectiveAppConfig: Config, reference: Config): Config = {
    import scala.collection.JavaConverters._

    ConfigFactory.parseMap(effectiveAppConfig.root().unwrapped().asScala.filterKeys(reference.hasPath).asJava)
  }

  private final def classloader(): ClassLoader = {
    Some(Thread.currentThread().getContextClassLoader)
      .getOrElse(ClassLoader.getSystemClassLoader)
  }
}
