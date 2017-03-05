package org.bitbucket.pshirshov.izumitk.plugins

import com.google.inject.{Provides, Singleton}
import com.google.inject.name.Named
import org.bitbucket.pshirshov.izumitk.cdi.GuicePlugin
import org.bitbucket.pshirshov.izumitk.cluster.ClusterUtils
import org.bitbucket.pshirshov.izumitk.cluster.model.{HostId, NodeAddress}

class ClusterNodeIdSimplePlugin extends GuicePlugin {
  override def configure(): Unit = {
  }

  @Named("clustering.node.id")
  @Provides
  @Singleton
  def hostId: HostId = ClusterUtils.hostId

  @Named("clustering.node.address")
  @Provides
  @Singleton
  def localNodeAddress(
                        @Named("clustering.node.id") hostId: HostId
                        , @Named("@clustering.dc.local.id") dcId: String
                      ): NodeAddress = NodeAddress(hostId, dcId)
}
