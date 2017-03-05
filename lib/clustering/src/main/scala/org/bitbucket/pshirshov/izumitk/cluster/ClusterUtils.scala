package org.bitbucket.pshirshov.izumitk.cluster

import java.net.NetworkInterface

import com.google.common.hash.Hashing
import org.bitbucket.pshirshov.izumitk.cluster.model.HostId

import scala.collection.JavaConverters._

object ClusterUtils {
  def hostId: HostId = {
    // this is a host identifier which should persist between app restarts
    val hasher = Hashing.murmur3_128().newHasher

    NetworkInterface.getNetworkInterfaces.asScala.foreach {
      i =>
        Option(i.getHardwareAddress) match {
          case Some(mac) =>
            hasher.putBytes(mac)
          case None =>
        }
    }

    HostId(hasher.hash().toString)
  }
}
