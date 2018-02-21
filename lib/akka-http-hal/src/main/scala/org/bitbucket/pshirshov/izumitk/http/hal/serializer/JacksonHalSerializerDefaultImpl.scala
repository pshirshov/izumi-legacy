package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.{Inject, Singleton}
import com.theoryinpractise.halbuilder5.ResourceRepresentation
import com.theoryinpractise.halbuilder5.json.JsonRepresentationWriter

import scala.reflect.runtime._

@Singleton
class JacksonHalSerializerDefaultImpl @Inject()(jsonRepresentationWriter: JsonRepresentationWriter)
  extends JacksonHalSerializer {
  override def valueToTree[T](repr: ResourceRepresentation[T]): ObjectNode = {
    val renderJson = jsonRepresentationWriter.getClass.getDeclaredMethod("renderJson"
      , currentMirror.runtimeClass(universe.typeOf[ResourceRepresentation[_]])
      , currentMirror.runtimeClass(universe.typeOf[Boolean]))

    renderJson.setAccessible(true)
    renderJson.invoke(jsonRepresentationWriter, repr, Boolean.box(false)).asInstanceOf[ObjectNode]
  }

  override def writeValueAsString[T](repr: ResourceRepresentation[T]): String =
    jsonRepresentationWriter.print(repr).utf8()
}
