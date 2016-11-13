package org.bitbucket.pshirshov.izumitk.cdi

import com.google.inject.Module
import com.google.inject.util.Modules
import com.typesafe.scalalogging.StrictLogging

case class BunchOfModules(name: String, modules: Seq[Module]) {
  override def toString: String = s"[$name]={${modules.map(ModuleUtil.shortModuleName).mkString(", ")}}"

  def asOption: Option[BunchOfModules] = {
    modules.isEmpty match {
      case false =>
        Some(this)
      case true =>
        None
    }
  }
}

object ModuleUtil extends StrictLogging {
  def multipleOverride(allModules: Seq[BunchOfModules]): Module = {
    import scala.collection.JavaConverters._

    allModules.flatMap(_.asOption).foldLeft(Modules.EMPTY_MODULE) {
      case (acc, modules) =>
        logger.debug(s"Injector: overriding `${shortModuleName(acc)}` with `$modules`")
        val result = Modules.`override`(acc).`with`(modules.modules.asJava)
        logger.trace(s"Got new module: ${shortModuleName(result)}")
        result
    }
  }

  protected[cdi] def shortModuleName(module: Module) = {
    s"${module.getClass.getSimpleName}@${module.hashCode()}"
  }
}
