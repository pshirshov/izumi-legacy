package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import akka.http.scaladsl.model.HttpRequest
import com.theoryinpractise.halbuilder.api.Representation
import org.bitbucket.pshirshov.izumitk.http.hal.model.HalContext

trait HalSerializer {
  def makeRepr(dto: Any, hc: HalContext, rc: HttpRequest): Representation
}
