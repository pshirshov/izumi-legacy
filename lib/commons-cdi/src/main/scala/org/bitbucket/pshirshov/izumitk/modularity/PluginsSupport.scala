package org.bitbucket.pshirshov.izumitk.modularity

import com.google.common.reflect.ClassPath
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.RequiredConfig
import org.bitbucket.pshirshov.izumitk.cdi.Plugin
import org.bitbucket.pshirshov.izumitk.modularity.tools.WithPluginsConfig
import org.scalactic._

import scala.collection.JavaConverters._





trait PluginsSupport
  extends WithPluginsPackages
    with WithPluginsConfig
    with StrictLogging {

  def loadPlugins(): Seq[Plugin] = {
    if (!pluginsConfig.enabled) {
      logger.warn("Plugins support is disabled")
      return Seq()
    }

    logger.debug(s"Scanning `${pluginsPackages()}` for plugins...")

    val classpath = ClassPath.from(Thread.currentThread().getContextClassLoader)
    val plugins = loadPlugins(pluginsPackages(), classpath)

    logger.debug(s"Plugins loaded: ${plugins.size}")
    plugins.sorted
  }

  def loadPlugins(pluginsPackage: Seq[String], classpath: ClassPath): Seq[Plugin] = {
    val allLoadabalePluginClasses =
      pluginsPackage.flatMap {
        pkg =>
          classpath
            .getTopLevelClassesRecursive(pkg)
            .asScala.map(_.load())
      }

    val activeAndValidPluginClasses = filterPluginClasses(allLoadabalePluginClasses)

    val loadedPlugins = activeAndValidPluginClasses.flatMap {
      clz =>
        logger.trace(s"Processing plugin `${clz.getCanonicalName}`...")

        val instance = loadConfigurablePlugin(clz).recoverWith {
          bad =>
            loadSimplePlugin(clz).badMap(bad1 => bad ++ bad1)
        }

        instance match {
          case Good(p) =>
            logger.info(s"Plugin `${clz.getCanonicalName}` instantiated: $p")
            Some(p)

          case Bad(f) =>
            logger.error(s"Loading of plugin `${clz.getCanonicalName}` failed: $f")
            None
        }
    }

    loadedPlugins
  }

  protected def filterPluginClasses(classes: Seq[Class[_]]): Seq[Class[_]] = classes

  private def loadSimplePlugin(clz: Class[_]): Or[Plugin, Every[Throwable]] = {
    try {
      Good(clz.getConstructor().newInstance().asInstanceOf[Plugin])
    } catch {
      case t: Throwable =>
        Bad(One(t))
    }
  }

  protected def loadConfigurablePlugin(clz: Class[_]): Or[Plugin, Every[Throwable]] = {
    try {
      val constructor = clz.getConstructor(classOf[Config])
      val configAnnotation = constructor.getAnnotation(classOf[RequiredConfig])
      if (configAnnotation == null) {
        throw new IllegalStateException(s"Plugin doesn't have a constructor annotated with @RequiredConfig")
      }

      val declaredConfigSection = configAnnotation.value()

      val pluginConfig = pluginConfigSection(clz, declaredConfigSection)

      Good(constructor.newInstance(pluginConfig).asInstanceOf[Plugin])
    } catch {
      case t: Throwable =>
        Bad(One(t))
    }
  }
}

