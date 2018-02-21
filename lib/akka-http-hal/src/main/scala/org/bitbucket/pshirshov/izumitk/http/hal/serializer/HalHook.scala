package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import com.fasterxml.jackson.databind.node.ObjectNode
import com.theoryinpractise.halbuilder5.ResourceRepresentation
import org.bitbucket.pshirshov.izumitk.http.hal.model.HalEntityContext

trait HalHook {
  def handleEntity: PartialFunction[HalEntityContext, ResourceRepresentation[ObjectNode]]

  def reduce: PartialFunction[HalEntityContext, HalEntityContext] = PartialFunction.empty
}
