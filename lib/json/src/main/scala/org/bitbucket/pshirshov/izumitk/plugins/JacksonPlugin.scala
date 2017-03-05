package org.bitbucket.pshirshov.izumitk.plugins

import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.cdi.Plugin
import org.bitbucket.pshirshov.izumitk.json.modules.JacksonModule

class JacksonPlugin extends Plugin {
  override def createPluginModules: Seq[ScalaModule] = Seq(
    new JacksonModule()
  )
}
