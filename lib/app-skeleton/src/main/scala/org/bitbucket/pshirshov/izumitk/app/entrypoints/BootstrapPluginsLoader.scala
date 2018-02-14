package org.bitbucket.pshirshov.izumitk.app.entrypoints

import org.bitbucket.pshirshov.izumitk.config.LoadedConfig
import org.bitbucket.pshirshov.izumitk.modularity.PluginsSupport
import org.bitbucket.pshirshov.izumitk.modularity.tools.DefaultPluginsConfigService

case class BootstrapPluginsLoader
(
  override protected val basePackage: String
  , config: LoadedConfig
)
  extends PluginsSupport {
  override protected def namespace: String = "entrypoints"

  override protected def pluginsConfigService = new DefaultPluginsConfigService(config)
}