package org.bitbucket.pshirshov.izumitk.cluster.model

import org.bitbucket.pshirshov.izumitk.model.Identifier

case class HostId(id: String) extends Identifier {
  override def asString: String = s"host#$id"
}
