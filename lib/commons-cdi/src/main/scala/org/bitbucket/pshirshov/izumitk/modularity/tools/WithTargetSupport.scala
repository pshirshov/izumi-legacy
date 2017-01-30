package org.bitbucket.pshirshov.izumitk.modularity.tools

import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.TargetPoint
import org.bitbucket.pshirshov.izumitk.cdi.Plugin
import org.bitbucket.pshirshov.izumitk.modularity.PluginsSupport

/**
  */
protected[modularity] trait WithTargetSupport
  extends StrictLogging {

  this: PluginsSupport =>

  abstract protected override def filterPluginClasses(classes: Seq[Class[_]]): Seq[Class[_]] = {
    classes.filter(isValidTarget)
  }

  protected def isValidTarget(pclass: Class[_]): Boolean = {
    classOf[Plugin].isAssignableFrom(pclass) &&
      !pluginDeactivated(pclass) &&
      pluginIsValidTarget(pclass)
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

}
