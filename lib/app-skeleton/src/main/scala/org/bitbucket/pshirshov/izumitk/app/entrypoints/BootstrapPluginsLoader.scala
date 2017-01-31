package org.bitbucket.pshirshov.izumitk.app.entrypoints

import org.bitbucket.pshirshov.izumitk.config.LoadedConfig
import org.bitbucket.pshirshov.izumitk.modularity.PluginsSupport

class BootstrapPluginsLoader
(
  override protected val basePackage: Package
  , override protected val appId: String
  , override protected val config: LoadedConfig
)
  extends PluginsSupport {
  override protected def namespace: String = "entrypoints"
}
