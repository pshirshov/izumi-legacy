package org.bitbucket.pshirshov.izumitk.plugins

import java.util.regex.Pattern

import com.google.common.reflect.ClassPath
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.cdi.Plugin
import org.bitbucket.pshirshov.izumitk.config.LoadedConfig
import org.bitbucket.pshirshov.izumitk.util.{CollectionUtils, StringUtils}
import org.bitbucket.pshirshov.izumitk.{Depends, NonRootPlugin, RequiredConfig, TargetPoint}
import org.scalactic._

import scala.collection.JavaConversions._

case class PluginsConfig(
                          enabled: Boolean
                          , deactivated: Set[String]
                          , targets: Config
                        )

trait PluginsSupport extends StrictLogging {
  protected val config: LoadedConfig

  protected lazy val appId: String = "noapp" // override in implementation to support per-application plugin scan

  protected def loadPlugins(): Seq[Plugin] = {
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
            .map(_.load())
      }

    val activeAndValidPluginClasses = allLoadabalePluginClasses.filter {
      pclass =>
        classOf[Plugin].isAssignableFrom(pclass) &&
          !pluginDeactivated(pclass) &&
          pluginIsValidTarget(pclass)
    }

    val dependencyEdges = activeAndValidPluginClasses.flatMap {
      clz =>
        Option(clz.getAnnotation(classOf[Depends])) match {
          case Some(ann) =>
            ann.value().map(dependency => clz -> dependency)
          case None =>
            Seq()
        }
    }

    val dependencies = CollectionUtils.toMapOfSets[Class[_], Class[_]](dependencyEdges)
    val reverseDependencies = CollectionUtils.toMapOfSets[Class[_], Class[_]](dependencyEdges.map(_.swap))

    logger.debug(s"Dependency matrix: $dependencies")
    logger.debug(s"Reverse dependency matrix: $reverseDependencies")

    def withoutUnrequiredClasses = activeAndValidPluginClasses.filter {
      clz =>
        Option(clz.getAnnotation(classOf[NonRootPlugin])) match {
          case Some(ann) =>
            val classes = (clz.getInterfaces :+ clz).toSet
            // TODO: here we need to check all the dependency graph, not only first dependency level
            val isOk = reverseDependencies.keySet.intersect(classes).nonEmpty
            logger.debug(s"Non-root plugin `$clz` implements $classes and is necessary = $isOk")
            isOk
          case None =>
            true
        }
    }

    val loadedPlugins = withoutUnrequiredClasses.flatMap {
      clz =>
        logger.debug(s"Processing plugin `${clz.getCanonicalName}`...")

        val instance = loadConfigurablePlugin(clz).recoverWith {
          bad =>
            loadSimplePlugin(clz).badMap(bad1 => bad ++ bad1)
        }

        instance match {
          case Good(p) =>
            logger.debug(s"Plugin `${clz.getCanonicalName}` instantiated: $p")
            Some(p)

          case Bad(f) =>
            logger.warn(s"Loading of plugin `${clz.getCanonicalName}` failed: $f")
            None
        }
    }

    loadedPlugins
  }

  protected def pluginDeactivated(pclass: Class[_]): Boolean = {
    if (
      pluginsConfig.deactivated.contains(pclass.getCanonicalName) ||
        pluginsConfig.deactivated.contains(pclass.getSimpleName) ||
        pluginsConfig.deactivated.exists { expr =>
          expr.startsWith("rx:") && Pattern.compile(expr.substring(3)).matcher(pclass.getCanonicalName).matches()
        }
    ) {
      logger.debug(s"Plugin is deactivated: $pclass")
      true
    } else {
      false
    }
  }

  protected def pluginIsValidTarget(pclass: Class[_]): Boolean = {
    pclass.getInterfaces.filter(_.isAnnotationPresent(classOf[TargetPoint])).toList match {
      case Nil =>
        logger.trace(s"Plugin is not a target point: $pclass")
        true

      case head :: Nil =>
        if (!pluginsConfig.targets.hasPath(head.getCanonicalName) && !pluginsConfig.targets.hasPath(head.getSimpleName)) {
          throw new IllegalStateException(s"No target defined in config for: ${head.getCanonicalName}")
        }

        if (validTarget(pclass, head.getCanonicalName) || validTarget(pclass, head.getSimpleName)) {
          logger.debug(s"Plugin is a valid target point: $pclass")
          true
        } else {
          logger.debug(s"Plugin is NOT a valid target point: $pclass")
          false
        }

      case interfaces@(head :: tail) =>
        throw new IllegalStateException(s"Class must implement only one extension point: $pclass <-- $interfaces")
    }
  }

  private def validTarget(pclass: Class[_], target: String): Boolean = {
    pluginsConfig.targets.hasPath(target) && (
      pluginsConfig.targets.getString(target) == pclass.getCanonicalName ||
        pluginsConfig.targets.getString(target) == pclass.getSimpleName
      )
  }

  protected def pluginsPackages() = {
    val companyPackage = getClass.getPackage.getName.split('.').take(2).toList.mkString(".")

    Seq(
      s"org.bitbucket.pshirshov.izumitk.plugins"
      , s"$companyPackage.plugins"
      , s"$companyPackage.$appId.plugins"
      , s"${getClass.getPackage.getName}.plugins"
    ) // lookup in org.bitbucket.pshirshov.izumitk and org.bitbucket.pshirshov.izumitk.<appId>
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

      val pluginConfig: Config = if (declaredConfigSection == "*") {
        logger.debug(s"Plugin `${clz.getCanonicalName}` will use full app config")
        config.effective
      } else if (declaredConfigSection.nonEmpty) {
        logger.debug(s"Plugin `${clz.getCanonicalName}` will use config section `$declaredConfigSection`")
        config.effective.getConfig(declaredConfigSection)
      } else {
        val fullName = s"plugins.config.${clz.getCanonicalName}"
        val shortName = s"plugins.config.${clz.getSimpleName}"
        if (config.effective.hasPath(fullName)) {
          logger.debug(s"Plugin `${clz.getCanonicalName}` will use config section `$fullName`")
          config.effective.getConfig(fullName)
        } else {
          logger.debug(s"Plugin `${clz.getCanonicalName}` will use config section `$shortName`")
          config.effective.getConfig(shortName)
        }
      }

      if (pluginConfig == null) {
        throw new IllegalStateException(s"Expected config section not found for `${clz.getCanonicalName}`")
      }

      Good(constructor.newInstance(pluginConfig).asInstanceOf[Plugin])
    } catch {
      case t: Throwable =>
        Bad(One(t))
    }
  }

  protected lazy val pluginsConfig = {
    val pluginsSection = config.effective.getConfig("plugins")
    val deactivated = pluginsSection.getStringList("deactivated").toSet
    val enabled = StringUtils.toBoolean(System.getProperty("plugins.enabled"))
      .getOrElse(pluginsSection.getBoolean("enabled"))
    val targets = pluginsSection.getConfig("targets")
    PluginsConfig(enabled, deactivated, targets)
  }

}
