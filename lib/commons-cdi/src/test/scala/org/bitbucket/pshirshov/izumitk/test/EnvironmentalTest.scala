package org.bitbucket.pshirshov.izumitk.test

import com.google.inject.Module
import com.google.inject.util.Modules
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.TestConfig.{TestConfigSection, references}
import org.bitbucket.pshirshov.izumitk.TestConfigExtensions
import org.bitbucket.pshirshov.izumitk.app.modules.ConfigExposingModule
import org.bitbucket.pshirshov.izumitk.cdi.GuicePlugin

/**
  */
@ExposedTestScope
trait EnvironmentalTest
  extends InjectorTestBase
    with TestConfigExtensions {

  override protected val modules: Module = {
    val referencesToUse = verifyReferences(requiredReferences.toSet)

    val mainModules = verifyModules(environment ++ Seq(
      new ConfigExposingModule(references(referencesToUse.toSeq: _*))
    ))

    val environmentOverrides = verifyModules(overrides)

    Modules
      .`override`(mainModules: _*)
      .`with`(environmentOverrides: _*)
  }

  protected def environment: Seq[Module] = Seq.empty

  protected def overrides: Seq[Module] = {
    configOverrides.toSeq match {
      case Nil =>
        Seq.empty

      case values =>
        Seq(new ConfigOverrideModule(values :_* ))
    }
  }

  protected def requiredReferences: Seq[TestConfigSection] = Seq.empty

  protected def configOverrides: Map[String, AnyRef] = Map.empty

  private def verifyReferences(referencesToUse: Set[TestConfigSection]): Set[TestConfigSection] = {
    val grouped = referencesToUse.groupBy(_.alias)
    val badAliases = grouped.filter(_._2.size != 1)
    if (badAliases.nonEmpty) {
      logger.warn(s"Bad aliases found. One alias must be assigned to exactly one resource: $badAliases")
      throw new IllegalStateException()
    }
    referencesToUse
  }

  private def verifyModules(mainModules: Seq[Module]): Seq[Module] = {
    mainModules.groupBy(_.getClass).map {
      case (clazz, modulesOfClass) =>
        if (modulesOfClass.size != 1) {
          logger.warn(s"DI module `$clazz` unexpectedly has not just one instance: $modulesOfClass")
        }
        modulesOfClass.head
    }.toSeq
  }

  protected def plugins(plugins: GuicePlugin*): Seq[ScalaModule] = plugins.flatMap(_.createPluginModules)
}
