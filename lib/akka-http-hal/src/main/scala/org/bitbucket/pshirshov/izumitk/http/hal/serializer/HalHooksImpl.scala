package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.{Inject, Singleton}
import com.theoryinpractise.halbuilder5.ResourceRepresentation
import org.bitbucket.pshirshov.izumitk.http.hal.model.HalEntityContext

@Singleton
class HalHooksImpl @Inject()
(
  hooks: Set[HalHook]
) extends HalHooks {
  private val identity = PartialFunction.apply[HalEntityContext, HalEntityContext](a => a)
  private val failure = PartialFunction.apply[HalEntityContext, ResourceRepresentation[ObjectNode]](a => throw new IllegalArgumentException(s"Unsupported context: $a"))

  override def handleEntity(ec: HalEntityContext): ResourceRepresentation[ObjectNode] = {
    val applicator = hooks.map(_.handleEntity).foldLeft(failure) {
      case (acc, v) =>
        v orElse acc
    }
    applicator(ec)
  }


  override def reduce(ec: HalEntityContext): HalEntityContext = {
    val reducer = hooks.map(_.reduce).foldLeft(identity) {
      case (acc, v) =>
        v orElse acc
    }
    reducer(ec)
  }
}
