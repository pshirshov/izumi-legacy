package org.bitbucket.pshirshov.izumitk.plugins

import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.app.modules.JmxModule
import org.bitbucket.pshirshov.izumitk.cdi.Plugin

class JmxPlugin extends Plugin {
  override def createPluginModules: Seq[ScalaModule] = Seq(
    new JmxModule()
  )
}
