package org.bitbucket.pshirshov.izumitk.plugins

import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.NonRootPlugin
import org.bitbucket.pshirshov.izumitk.cassandra.modules.CassandraModule
import org.bitbucket.pshirshov.izumitk.cdi.Plugin

/**
  */
@NonRootPlugin
class CassandraPlugin extends Plugin {
  override def createPluginModules: Seq[ScalaModule] = Seq(
    new CassandraModule
  )
}
