package org.bitbucket.pshirshov.izumitk.json.model

import com.fasterxml.jackson.databind.node.ObjectNode

trait WithMeta {
  def meta: ObjectNode
}
