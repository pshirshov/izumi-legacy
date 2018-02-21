package org.bitbucket.pshirshov.izumitk.http.hal.model

import akka.http.scaladsl.model.HttpRequest
import com.fasterxml.jackson.databind.node.ObjectNode
import com.theoryinpractise.halbuilder5.ResourceRepresentation

trait HalContext {}

case class HalExceptionContext(exception: Throwable) extends HalContext

case class HalRequestContext(baseUri: String, rc: HttpRequest)

case class HalEntityContext(
                             hc: HalContext
                             , rc: HalRequestContext
                             , dto: Any
                             , repr: ResourceRepresentation[ObjectNode]
                           )
