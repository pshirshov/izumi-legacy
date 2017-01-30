package org.bitbucket.pshirshov.izumitk.modularity

import com.google.common.reflect.ClassPath
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.RequiredConfig
import org.bitbucket.pshirshov.izumitk.cdi.Plugin
import org.bitbucket.pshirshov.izumitk.modularity.tools.WithPluginsConfig
import org.scalactic._

import scala.collection.JavaConverters._
import scala.collection.immutable.ListSet

/**
  */
trait PluginsSupport
  extends WithPluginsConfig
    with StrictLogging {

  protected lazy val appId: String = "noapp" // override in implementation to support per-application plugin scan

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
            logger.warn(s"Loading of plugin `${clz.getCanonicalName}` failed: $f")
            None
        }
    }

    loadedPlugins
  }

  protected def filterPluginClasses(classes: Seq[Class[_]]): Seq[Class[_]] = classes

  protected def pluginsPackages(): Seq[String] = {
    val pkgCompany = companyPackage()
    val pkgClass = classPackage()

    ListSet(
      s"org.bitbucket.pshirshov.izumitk.plugins"
      , s"$pkgCompany.plugins"
      , s"$pkgCompany.$appId.plugins"
      , s"$pkgClass.plugins"
    ).toSeq // lookup in org.bitbucket.pshirshov.izumitk and org.bitbucket.pshirshov.izumitk.<appId>
  }

  private def loadSimplePlugin(clz: Class[_]): Plugin Or Every[Throwable] = {
    try {
      Good(clz.getConstructor().newInstance().asInstanceOf[Plugin])
    } catch {
      case t: Throwable =>
        Bad(One(t))
    }
  }

  private def loadConfigurablePlugin(clz: Class[_]): Plugin Or Every[Throwable] = {
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

  protected def companyPackage(): String = getClass.getPackage.getName.split('.').take(2).toList.mkString(".")

  protected def classPackage(): String = getClass.getPackage.getName
}
