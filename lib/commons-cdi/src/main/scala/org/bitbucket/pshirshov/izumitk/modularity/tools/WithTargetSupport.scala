package org.bitbucket.pshirshov.izumitk.modularity.tools

import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.cdi.Plugin
import org.bitbucket.pshirshov.izumitk.modularity.PluginsSupport
import org.bitbucket.pshirshov.izumitk.{Suppresses, TargetPoint}

/**
  */
protected[modularity] trait WithTargetSupport
  extends StrictLogging {

  this: PluginsSupport =>

  abstract protected override def filterPluginClasses(classes: Seq[Class[_]]): Seq[Class[_]] = {
    val activeAndValid = classes.filter(isValidTarget)

    val overrides = activeAndValid
      .filter(_.isAnnotationPresent(classOf[Suppresses])).flatMap {
      _.getAnnotation(classOf[Suppresses]).value()
    }

    val (toSuppress, toUse) = activeAndValid.partition { p =>
      overrides.contains(p.getCanonicalName)
    }

    toSuppress.foreach {
      p =>
        logger.debug(s"Plugin has been suppressed by another one: $p")
    }

    toUse
  }

  protected def isValidTarget(pclass: Class[_]): Boolean = {
    classOf[Plugin].isAssignableFrom(pclass) &&
      !pluginsConfigService.isPluginDeactivated(pclass) &&
      pluginIsValidTarget(pclass)
  }

  protected def pluginIsValidTarget(pclass: Class[_]): Boolean = {
    pclass.getInterfaces.filter(_.isAnnotationPresent(classOf[TargetPoint])).toList match {
      case Nil =>
        logger.trace(s"Plugin is not a target point: $pclass")
        true

      case head :: Nil =>
        val targets = pluginsConfigService.pluginsConfig.targets
        if (!targets.hasPath(head.getCanonicalName) && !targets.hasPath(head.getSimpleName)) {
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
    val targets = pluginsConfigService.pluginsConfig.targets

    targets.hasPath(target) && (
      targets.getString(target) == pclass.getCanonicalName ||
        targets.getString(target) == pclass.getSimpleName
      )
  }

}
