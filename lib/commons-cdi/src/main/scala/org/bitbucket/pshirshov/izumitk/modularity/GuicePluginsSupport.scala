package org.bitbucket.pshirshov.izumitk.modularity

import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.app.modules.AppConstantsModule
import org.bitbucket.pshirshov.izumitk.cdi.{BootstrapPlugin, BunchOfModules, Plugin}
import org.bitbucket.pshirshov.izumitk.modularity.model.PluginsInitiated
import org.bitbucket.pshirshov.izumitk.modularity.tools.PluginsIntrospectionModule




object GuicePluginsSupport {
  def filterModulesSequence(plugins: Seq[Plugin], modules: BunchOfModules): BunchOfModules = {
    val bootstrapPlugins = plugins
      .filter(_.isInstanceOf[BootstrapPlugin])
      .map(_.asInstanceOf[BootstrapPlugin])

    BunchOfModules(s"filtered:${modules.name}", bootstrapPlugins.foldLeft(modules.modules) {
      case (m, plugin) =>
        plugin.handleModulesList(m)
    })
  }

  def filterModules(plugins: Seq[Plugin], modules: Seq[BunchOfModules]): Seq[BunchOfModules] = {
    modules.map(filterModulesSequence(plugins, _))
  }
}



trait GuicePluginsSupport extends PluginsSupport with StrictLogging {
  protected def loadPluginModules(): PluginsInitiated = {
    val allPlugins = loadPlugins()

    val modules: BunchOfModules = BunchOfModules("plugins", allPlugins.flatMap(_.createPluginModules))
    logger.debug(s"Modules instantiated: ${modules.modules.size}: $modules")

    val internalModules = BunchOfModules("plugin-support", Seq(
      new AppConstantsModule(appId)
      , new PluginsIntrospectionModule(allPlugins)
    ))

    PluginsInitiated(allPlugins, Seq(modules, internalModules))
  }
}