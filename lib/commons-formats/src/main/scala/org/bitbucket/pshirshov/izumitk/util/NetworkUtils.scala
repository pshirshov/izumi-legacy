package org.bitbucket.pshirshov.izumitk.util

import java.net.NetworkInterface

import com.google.common.hash.Hashing
import scala.collection.JavaConverters._

object NetworkUtils {
  def hostId: String = {
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

    val hostId = hasher.hash().toString
    hostId
  }
}
