package org.bitbucket.pshirshov.izumitk.plugins

import com.google.inject.TypeLiteral
import com.google.inject.util.Types
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.ExtensionPoint
import org.bitbucket.pshirshov.izumitk.app.modules.AppConstantsModule
import org.bitbucket.pshirshov.izumitk.cdi.{BootstrapPlugin, BunchOfModules, Plugin}


case class PluginsInitiated(plugins: Seq[Plugin], modules: Seq[BunchOfModules]) {
  def filteredModules(externalModules: Seq[BunchOfModules]): Seq[BunchOfModules] = {
    GuicePluginsSupport.filterModules(plugins, externalModules ++ modules)
  }
}

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

class PluginsIntrospectionModule(allPlugins: Seq[Plugin]) extends ScalaModule with StrictLogging {
  override def configure(): Unit = {
    // this allows us to inject collection of all the plugins
    logger.debug(s"Binding all plugins as named Seq[Plugin] 'app.plugins' to $allPlugins...")
    bind[Seq[Plugin]].annotatedWithName("app.plugins").toInstance(allPlugins)

    // this allows us to inject single plugin by classname or type (very bad practice, tight coupling!)
    allPlugins.foreach {
      plugin =>
        val bindName = s"app.plugins.${plugin.pluginName}"
        logger.debug(s"Binding plugin `${plugin.pluginName}` to $plugin...")
        bind[Plugin].annotatedWithName(bindName).toInstance(plugin)
        logger.debug(s"Binding `${plugin.getClass}` to $plugin...")
        bind(plugin.getClass.asInstanceOf[Class[AnyRef]]).toInstance(plugin)
    }

    // this allows us to inject collections of plugins implementing interface marked with @ExtensionPoint
    val pluginsWithInterfaces = allPlugins
      .flatMap(p => p.getClass.getInterfaces.filter(_.isAnnotationPresent(classOf[ExtensionPoint])).map(i => (i, p)))

    val extenderSets = pluginsWithInterfaces.groupBy(e => e._1).mapValues(e => e.map(x => x._2).toSet)
    extenderSets.foreach {
      case (e, plugins) =>
        val tl = TypeLiteral.get(Types.newParameterizedType(classOf[Set[_]], e)).asInstanceOf[TypeLiteral[Set[_]]]
        logger.debug(s"Binding Set[$e] to $plugins...")
        bind(tl).toInstance(plugins)
    }
  }
}

trait GuicePluginsSupport extends PluginsSupport with StrictLogging {
  protected def loadPluginModules(): PluginsInitiated = {
    val allPlugins = loadPlugins()

    val modules: BunchOfModules = BunchOfModules("plugins", allPlugins.flatMap(_.createPluginModules))
    logger.debug(s"Modules instantiated: ${modules.modules.size}: $modules")

    val internalModules = BunchOfModules("plugin-support", Seq(
      new PluginsIntrospectionModule(allPlugins), new AppConstantsModule(appId)
    ))

    PluginsInitiated(allPlugins, Seq(modules, internalModules))
  }
}
