package org.bitbucket.pshirshov.izumitk.modularity.tools

import com.google.inject.TypeLiteral
import com.google.inject.util.Types
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.ExtensionPoint
import org.bitbucket.pshirshov.izumitk.cdi.Plugin

/**
  */
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
