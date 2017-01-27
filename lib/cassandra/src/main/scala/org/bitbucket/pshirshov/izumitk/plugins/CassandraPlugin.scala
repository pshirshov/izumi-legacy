package org.bitbucket.pshirshov.izumitk.plugins

import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.cassandra.modules.{CassandraFacadeConfigModule, CassandraModule}
import org.bitbucket.pshirshov.izumitk.cdi.Plugin

/**
  */
class CassandraPlugin extends Plugin {
  override def createPluginModules: Seq[ScalaModule] = Seq(
    new CassandraModule()
    , new CassandraFacadeConfigModule()
  )
}
