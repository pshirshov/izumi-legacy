package org.bitbucket.pshirshov.izumitk.http.hal

import com.theoryinpractise.halbuilder.api.{Representation, RepresentationFactory}

/**
  */
trait HalEntity {
  def hal(factory: RepresentationFactory, baseUrl: String): Representation
}
