package org.bitbucket.pshirshov.izumitk.plugins

import java.net.InetAddress

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import org.apache.commons.lang3.RandomStringUtils
import org.bitbucket.pshirshov.izumitk.cdi.GuicePlugin
import org.bitbucket.pshirshov.izumitk.cluster.ClusterUtils
import org.bitbucket.pshirshov.izumitk.cluster.model.HostId

class ClusterNodeIdSimplePlugin extends GuicePlugin {
  override def configure(): Unit = {
  }

  @Named("clustering.node.id")
  @Provides
  @Singleton
  def hostId(@Named("@clustering.node.name") nodename: String): HostId = {
    nodename match {
      case "interface" =>
        HostId(s"node:if:${ClusterUtils.hostId.id}")

      case "hostname" =>
        HostId(s"node:host:${InetAddress.getLocalHost.getHostName}")

      case "hostname-randomized" =>
        HostId(s"node:host:${InetAddress.getLocalHost.getHostName}:${RandomStringUtils.randomAlphanumeric(8)}")

      case "interface-randomized" =>
        HostId(s"node:if:${ClusterUtils.hostId.id}:${RandomStringUtils.randomAlphanumeric(8)}")

      case "random" =>
        HostId(s"node:rnd:${RandomStringUtils.randomAlphanumeric(8)}")

      case o =>
        HostId(o)
    }

  }

  @Named("clustering.node.id")
  @Provides
  @Singleton
  def hostId: HostId = ClusterUtils.hostId

}
