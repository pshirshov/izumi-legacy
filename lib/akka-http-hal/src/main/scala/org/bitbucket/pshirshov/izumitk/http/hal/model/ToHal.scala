package org.bitbucket.pshirshov.izumitk.http.hal.model

import com.theoryinpractise.halbuilder.api.Representation


sealed trait ToHal

object ToHal {

  case class Repr(representation: Representation) extends ToHal

  case class Auto[T](entity: T, hc: HalContext) extends ToHal

}




