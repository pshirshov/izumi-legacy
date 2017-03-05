package org.bitbucket.pshirshov.izumitk.plugins

import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.akka.modules.AkkaModule
import org.bitbucket.pshirshov.izumitk.cdi.Plugin

class AkkaPlugin extends Plugin {
  override def createPluginModules: Seq[ScalaModule] = Seq(
    new AkkaModule()
  )
}
