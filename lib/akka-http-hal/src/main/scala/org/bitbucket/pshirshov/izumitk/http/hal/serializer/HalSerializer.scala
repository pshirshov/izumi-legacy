package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import akka.http.scaladsl.model.HttpRequest
import com.fasterxml.jackson.databind.node.ObjectNode
import com.theoryinpractise.halbuilder5.ResourceRepresentation
import org.bitbucket.pshirshov.izumitk.http.hal.model.HalContext

trait HalSerializer {
  def makeRepr(dto: Any, hc: HalContext, rc: HttpRequest): ResourceRepresentation[ObjectNode]
}
