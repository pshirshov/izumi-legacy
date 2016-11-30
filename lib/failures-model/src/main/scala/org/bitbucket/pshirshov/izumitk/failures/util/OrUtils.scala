package org.bitbucket.pshirshov.izumitk.failures.util

import org.bitbucket.pshirshov.izumitk.failures.model.{Maybe, ServiceException, ServiceFailure}
import org.scalactic.{Every, One, Or}

import scala.util.Try

/**
  */
object OrUtils {
  def from[G](theTry: Try[G]): Maybe[G] = {
    from(theTry, "Call unexpectedly failed")
  }

  def from[G](theTry: Try[G], failureMessage: String): Maybe[G] = {
    from(theTry, mapException(Some(failureMessage)))
  }

  def from[G](theTry: Try[G], mapper: PartialFunction[Throwable,Every[ServiceFailure]]): Maybe[G] = {
    Or.from(theTry)
      .badMap(mapper)
  }

  def mapException(failureMessage: Option[String] = None): PartialFunction[Throwable, Every[ServiceFailure]] = {
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
