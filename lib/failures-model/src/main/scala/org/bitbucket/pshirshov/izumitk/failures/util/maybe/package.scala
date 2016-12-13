package org.bitbucket.pshirshov.izumitk.failures.util

import org.bitbucket.pshirshov.izumitk.failures.model.{Maybe, ServiceException, ServiceFailure}
import org.scalactic.{Every, One, Or}

import scala.util.Try

/**
  */
package object maybe {
  def from[G](theTry: => Try[G]): Maybe[G] = {
    from("Call unexpectedly failed")(theTry)
  }

  def from[G](failureMessage: String)(theTry: => Try[G]): Maybe[G] = {
    from(mapException(Some(failureMessage)))(theTry)
  }

  def from[G](mapper: PartialFunction[Throwable, Every[ServiceFailure]])(theTry: => Try[G]): Maybe[G] = {
    Or.from(theTry).badMap(mapper)
  }

  def flatten[G](theTry: => Try[Maybe[G]]): Maybe[G] = {
    flatten("Call unexpectedly failed")(theTry)
  }

  def flatten[G](failureMessage: String)(theTry: => Try[Maybe[G]]): Maybe[G] = {
    flatten(maybe.mapException(Some(failureMessage)))(theTry)
  }

  def flatten[G](mapper: PartialFunction[Throwable, Every[ServiceFailure]])(theTry: => Try[Maybe[G]]): Maybe[G] = {
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

  implicit class MaybeExtensions[T](theTry: Try[T]) {
    def maybe: Maybe[T] = from(theTry)
    def maybe(failureMessage: String): Maybe[T] = from(failureMessage)(theTry)
    def maybe(mapper: PartialFunction[Throwable, Every[ServiceFailure]]): Maybe[T] = from(mapper)(theTry)
  }
}
