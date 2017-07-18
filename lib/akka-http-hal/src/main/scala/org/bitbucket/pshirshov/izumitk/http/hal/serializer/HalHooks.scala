package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import org.bitbucket.pshirshov.izumitk.http.hal.model.HalEntityContext

trait HalHooks {
  def handleEntity(ec: HalEntityContext): Unit

  def reduce(ec: HalEntityContext): HalEntityContext
}
