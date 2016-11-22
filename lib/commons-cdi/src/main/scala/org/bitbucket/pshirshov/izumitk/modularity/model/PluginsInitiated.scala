package org.bitbucket.pshirshov.izumitk.modularity.model

import org.bitbucket.pshirshov.izumitk.cdi.{BunchOfModules, Plugin}
import org.bitbucket.pshirshov.izumitk.modularity.GuicePluginsSupport

/**
  */
case class PluginsInitiated(plugins: Seq[Plugin], modules: Seq[BunchOfModules]) {
  def filteredModules(externalModules: Seq[BunchOfModules]): Seq[BunchOfModules] = {
    GuicePluginsSupport.filterModules(plugins, externalModules ++ modules)
  }
}
