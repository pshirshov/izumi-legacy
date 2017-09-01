package org.bitbucket.pshirshov.izumitk.modularity.tools

import java.util.regex.Pattern

import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.Disabled
import org.bitbucket.pshirshov.izumitk.config.LoadedConfig
import org.bitbucket.pshirshov.izumitk.modularity.model.PluginsConfig
import org.bitbucket.pshirshov.izumitk.util.types.StringUtils

import scala.collection.JavaConverters._


class DefaultPluginsConfigService(override val appConfig: LoadedConfig) extends PluginsConfigService with StrictLogging {
  override val pluginsConfig: PluginsConfig = {
    val pluginsSection = appConfig.effective.getConfig("plugins")
    val deactivated = pluginsSection.getStringList("deactivated").asScala.toSet
    val activated = pluginsSection.getStringList("activated").asScala.toSet
    val enabled = StringUtils.toBoolean(System.getProperty("plugins.enabled"))
      .getOrElse(pluginsSection.getBoolean("enabled"))
    val targets = pluginsSection.getConfig("targets")
    PluginsConfig(enabled, deactivated, activated, targets)
  }

  override def isPluginDeactivated(pclass: Class[_]): Boolean = {
    val deactivated = pluginsConfig.deactivated
    val activated = pluginsConfig.activated

    val explicitlyActivated = isPluginIsListed(pclass, activated)
    val explicitlyDeactivated = isPluginIsListed(pclass, deactivated)
    val deactivatedByAnnotation = pclass.isAnnotationPresent(classOf[Disabled])

    val result = pclass match {
      case _ if deactivatedByAnnotation && !explicitlyActivated =>
        true

      case _ if deactivatedByAnnotation && explicitlyActivated =>
        false

      case _ if explicitlyDeactivated =>
        true

      case _ =>
        false
    }

    if (result) {
      logger.debug(s"Plugin is deactivated: $pclass")
    }

    result
  }

  override def isPluginIsListed(pclass: Class[_], list: Set[String]): Boolean = {
    list.contains(pclass.getCanonicalName) ||
      list.contains(pclass.getSimpleName) ||
      list.exists { expr =>
        expr.startsWith("rx:") && Pattern.compile(expr.substring(3)).matcher(pclass.getCanonicalName).matches()
      }
  }

  override def createPluginConfigSection(clz: Class[_], declaredConfigSection: String): Config = {
    val effective = appConfig.effective

    val pluginConfig: Config = if (declaredConfigSection == "*") {
      logger.debug(s"Plugin `${clz.getCanonicalName}` will use full app config")
      effective
    } else if (declaredConfigSection.nonEmpty) {
      logger.debug(s"Plugin `${clz.getCanonicalName}` will use config section `$declaredConfigSection`")
      effective.getConfig(declaredConfigSection)
    } else {
      val fullName = s"plugins.config.${clz.getCanonicalName}"
      val shortName = s"plugins.config.${clz.getSimpleName}"
      if (effective.hasPath(fullName)) {
        logger.debug(s"Plugin `${clz.getCanonicalName}` will use config section `$fullName`")
        effective.getConfig(fullName)
      } else {
        logger.debug(s"Plugin `${clz.getCanonicalName}` will use config section `$shortName`")
        effective.getConfig(shortName)
      }
    }

    if (pluginConfig == null) {
      throw new IllegalStateException(s"Expected config section not found for `${clz.getCanonicalName}`")
    }
    pluginConfig
  }
}
