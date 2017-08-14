package org.bitbucket.pshirshov.izumitk.cluster.model

import org.bitbucket.pshirshov.izumitk.model.Identifier


case class NodeAddress(id: NodeId, dc: DCId) extends Identifier {
  override def asString: String = s"$dc:${id.id}"
}

object NodeAddress {
  def parse(s: String): NodeAddress = {
    val parts = s.split(':')
    val identifier = parts.tail.mkString(":")
    NodeAddress(HostId(identifier), parts.head)
  }

  def asString(addr: NodeAddress): String = {
    s"${addr.dc}:${addr.id.id}"
  }
}

