package org.bitbucket.pshirshov.izumitk.failures.util

import org.bitbucket.pshirshov.izumitk.failures.model.{ServiceException, ServiceFailure}
import org.scalactic.{Every, One, Or}

import scala.util.Try

/**
  */
object OrUtils {
  def from[G](theTry: Try[G]): Or[G, Every[ServiceFailure]] = {
    from(theTry, "Call unexpectedly failed")
  }

  def from[G](theTry: Try[G], failureMessage: String): Or[G, Every[ServiceFailure]] = {
    from(theTry, mapException(Some(failureMessage)))
  }

  def from[G](theTry: Try[G], mapper: PartialFunction[Throwable,Every[ServiceFailure]]): Or[G, Every[ServiceFailure]] = {
    Or.from(theTry)
      .badMap(mapper)
  }

  def mapException(failureMessage: Option[String] = None): PartialFunction[Throwable,Every[ServiceFailure]] = {
    case s: ServiceFailure =>
      One(s.toException)
    case t: Throwable =>
      failureMessage match {
        case Some(m) =>
          One(new ServiceException(m, Some(t)))

        case None =>
          One(new ServiceException(t.getMessage, Some(t)))
      }
  }
}
