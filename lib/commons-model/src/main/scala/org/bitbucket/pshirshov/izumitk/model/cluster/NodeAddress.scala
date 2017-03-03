/*
 * Copyright (c) 2016. Teckro, Ltd.
 * All rights reserved.
 */

package org.bitbucket.pshirshov.izumitk.model.cluster

import org.bitbucket.pshirshov.izumitk.model.Identifier


case class NodeAddress(id: NodeId, dc: DCId) extends Identifier {
  override def asString: String = s"$dc:$id"
}


