package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import org.bitbucket.pshirshov.izumitk.http.hal.model.HalEntityContext

trait HalHook {
  def handleEntity: PartialFunction[HalEntityContext, Unit]

  def reduce: PartialFunction[HalEntityContext, HalEntityContext] = PartialFunction.empty
}
