package org.bitbucket.pshirshov.izumitk.http.hal.model

import com.fasterxml.jackson.databind.node.ObjectNode
import com.theoryinpractise.halbuilder5.ResourceRepresentation


sealed trait ToHal

object ToHal {

  case class Repr(representation: ResourceRepresentation[ObjectNode]) extends ToHal

  case class Auto[T](entity: T, hc: HalContext) extends ToHal

}




