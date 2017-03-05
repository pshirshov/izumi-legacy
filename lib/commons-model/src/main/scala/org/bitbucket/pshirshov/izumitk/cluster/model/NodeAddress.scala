package org.bitbucket.pshirshov.izumitk.cluster.model

import org.bitbucket.pshirshov.izumitk.model.Identifier


case class NodeAddress(id: NodeId, dc: DCId) extends Identifier {
  override def asString: String = s"$dc:$id"
}


