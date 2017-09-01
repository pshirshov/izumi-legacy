package org.bitbucket.pshirshov.izumitk.modularity.tools

import com.typesafe.config.Config
import org.bitbucket.pshirshov.izumitk.config.LoadedConfig
import org.bitbucket.pshirshov.izumitk.modularity.model.PluginsConfig

trait PluginsConfigService {
  def appConfig: LoadedConfig

  def pluginsConfig: PluginsConfig

  def isPluginDeactivated(pclass: Class[_]): Boolean

  def isPluginIsListed(pclass: Class[_], list: Set[String]): Boolean

  def createPluginConfigSection(clz: Class[_], declaredConfigSection: String): Config
}
