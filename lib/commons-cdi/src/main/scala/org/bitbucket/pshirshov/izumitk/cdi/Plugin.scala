package org.bitbucket.pshirshov.izumitk.cdi

import com.google.inject.Module
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.Depends


trait Plugin extends Ordered[Plugin] {
  def pluginName: String = getClass.getCanonicalName

  def createPluginModules: Seq[ScalaModule] = Seq()

  override def compare(that: Plugin): Int = {
    // TODO: also reflect constructors
    val thatDepends = that.getClass.getAnnotation(classOf[Depends])
    val thisDepends = this.getClass.getAnnotation(classOf[Depends])

    if (thatDepends == null && thisDepends == null) {
      return 0
    }

    val thatDependsOnThis = if (thatDepends == null) {
      false
    } else {
      thatDepends.value().exists(c => c.isAssignableFrom(this.getClass))
    }

    val thisDependsOnThat = if (thisDepends == null) {
      false
    } else {
      thisDepends.value().exists(c => c.isAssignableFrom(that.getClass))
    }

    if (thatDependsOnThis && thisDependsOnThat) {
      throw new IllegalStateException(s"Circular dependency between $this and $that")
    }

    if (thatDependsOnThis) {
      return -1
    }

    if (thisDependsOnThat) {
      return 1
    }

    0
  }
}

trait BootstrapPlugin {
  this: Plugin =>

  def handleModulesList(modules: Seq[Module]): Seq[Module] = modules
}

trait GuicePlugin
  extends ScalaModule
    with Plugin {
  override def createPluginModules: Seq[ScalaModule] = Seq(this)
}
