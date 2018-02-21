package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import com.fasterxml.jackson.databind.node.ObjectNode
import com.theoryinpractise.halbuilder5.ResourceRepresentation

trait JacksonHalSerializer {
  def valueToTree[T](repr: ResourceRepresentation[T]): ObjectNode

  def writeValueAsString[T](repr: ResourceRepresentation[T]): String
}
