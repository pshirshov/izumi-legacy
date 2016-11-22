package org.bitbucket.pshirshov.izumitk.http.hal

import com.theoryinpractise.halbuilder.api.Representation
import org.bitbucket.pshirshov.izumitk.http.hal.Hal.HalHandler

/**
  */
trait HalSerializer {
  def makeRepr(
                baseUri: String
                , dto: Any
                , handler: HalHandler
              ): Representation

}
