package org.bitbucket.pshirshov.izumitk.failures.util

import org.bitbucket.pshirshov.izumitk.failures.model.{Maybe, ServiceException, ServiceFailure}
import org.scalactic.{Every, One, Or}

import scala.util.Try

/**
  */
package object maybe {
  def from[G](theTry: Try[G]): Maybe[G] = {
    from(theTry, "Call unexpectedly failed")
  }

  def from[G](theTry: Try[G], failureMessage: String): Maybe[G] = {
    from(theTry, mapException(Some(failureMessage)))
  }

  def from[G](theTry: Try[G], mapper: PartialFunction[Throwable, Every[ServiceFailure]]): Maybe[G] = {
    Or.from(theTry)
      .badMap(mapper)
  }

  def mapTry[G](theTry: Try[Maybe[G]]): Maybe[G] = {
    mapTry(theTry, "Call unexpectedly failed")
  }

  def mapTry[G](theTry: Try[Maybe[G]], failureMessage: String): Maybe[G] = {
    mapTry(theTry, maybe.mapException(Some(failureMessage)))
  }

  def mapTry[G](theTry: Try[Maybe[G]], mapper: PartialFunction[Throwable, Every[ServiceFailure]]): Maybe[G] = {
    Or.from(theTry)
      .badMap(mapper)
      .flatMap(v => v)
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
