package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import com.fasterxml.jackson.databind.node.ObjectNode
import com.theoryinpractise.halbuilder5.ResourceRepresentation
import org.bitbucket.pshirshov.izumitk.http.hal.model.HalEntityContext

trait HalHooks {
  def handleEntity(ec: HalEntityContext): ResourceRepresentation[ObjectNode]

  def reduce(ec: HalEntityContext): HalEntityContext
}
