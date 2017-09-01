package org.bitbucket.pshirshov.izumitk.modularity

import com.google.common.reflect.ClassPath
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.RequiredConfig
import org.bitbucket.pshirshov.izumitk.cdi.Plugin
import org.bitbucket.pshirshov.izumitk.modularity.tools.PluginsConfigService
import org.scalactic._

import scala.collection.JavaConverters._


trait PluginsSupport
  extends WithPluginsPackages
    with StrictLogging {

  protected def filterPluginClasses(classes: Seq[Class[_]]): Seq[Class[_]] = classes

  protected def pluginsConfigService: PluginsConfigService

  def loadPlugins(): Seq[Plugin] = {
    if (!pluginsConfigService.pluginsConfig.enabled) {
      logger.warn("Plugins support is disabled")
      return Seq()
    }

    logger.debug(s"Scanning `${pluginsPackages()}` for plugins...")

    val classpath = ClassPath.from(Thread.currentThread().getContextClassLoader)
    val plugins = loadPlugins(pluginsPackages(), classpath)

    logger.debug(s"Plugins loaded: ${plugins.size}")
    plugins.sorted
  }

  protected def loadPlugins(pluginsPackage: Seq[String], classpath: ClassPath): Seq[Plugin] = {
    val allLoadabalePluginClasses =
      pluginsPackage.flatMap {
        pkg =>
          classpath
            .getTopLevelClassesRecursive(pkg)
            .asScala.map(_.load())
      }

    val activeAndValidPluginClasses = filterPluginClasses(allLoadabalePluginClasses)

    activeAndValidPluginClasses.flatMap(loadPlugin)
  }

  protected def loadPlugin(clz: Class[_]): Option[Plugin] = {
    loadConfigurablePlugin(clz).recoverWith {
      bad =>
        loadSimplePlugin(clz).badMap(bad1 => bad ++ bad1)
    } match {
      case Good(p) =>
        logger.debug(s"Plugin `${clz.getCanonicalName}` instantiated: $p")
        Some(p)

      case Bad(f) =>
        logger.error(s"Loading of plugin `${clz.getCanonicalName}` failed: $f")
        None
    }
  }


  private def loadSimplePlugin(clz: Class[_]): Or[Plugin, Every[Throwable]] = {
    logger.trace(s"Processing plugin `${clz.getCanonicalName}` as simple plugin...")

    try {
      Good(clz.getConstructor().newInstance().asInstanceOf[Plugin])
    } catch {
      case t: Throwable =>
        Bad(One(t))
    }
  }

  protected def loadConfigurablePlugin(clz: Class[_]): Or[Plugin, Every[Throwable]] = {
    logger.trace(s"Processing plugin `${clz.getCanonicalName}` as configurable plugin...")

    try {
      val constructor = clz.getConstructor(classOf[Config])
      val configAnnotation = constructor.getAnnotation(classOf[RequiredConfig])
      if (configAnnotation == null) {
        throw new IllegalStateException(s"Plugin doesn't have a constructor annotated with @RequiredConfig")
      }

      val declaredConfigSection = configAnnotation.value()

      val pluginConfig = pluginsConfigService.createPluginConfigSection(clz, declaredConfigSection)

      Good(constructor.newInstance(pluginConfig).asInstanceOf[Plugin])
    } catch {
      case t: Throwable =>
        Bad(One(t))
    }
  }
}

