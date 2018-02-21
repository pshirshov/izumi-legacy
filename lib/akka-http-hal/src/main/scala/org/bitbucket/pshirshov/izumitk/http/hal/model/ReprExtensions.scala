package org.bitbucket.pshirshov.izumitk.http.hal.model

import com.theoryinpractise.halbuilder5.{Rel, ResourceRepresentation}

object ReprExtensions {

  implicit class Extensions[T](private val self: ResourceRepresentation[T]) {
    // _.withRel has to be called before _.withLink, otherwise you get "Rel is already declared." exception LMAO
    def link(rel: Rel, value: String): ResourceRepresentation[T] =
      if (self.getRels.containsKey(rel.rel())) {
        self.withLink(rel.rel(), value)
      } else {
      self.withRel(rel)
          .withLink(rel.rel(), value)
    }
  }
}
