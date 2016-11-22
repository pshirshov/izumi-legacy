package org.bitbucket.pshirshov.izumitk.http.hal

import com.fasterxml.jackson.databind.node.ObjectNode

/**
  */
trait HalDecoder {
  import scala.reflect.runtime.universe._

  def readHal[T: TypeTag : Manifest](halTree: ObjectNode): T
  def readHal[T: TypeTag : Manifest](source: String): T

}
