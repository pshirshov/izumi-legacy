package org.bitbucket.pshirshov.izumitk.failures.util

import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.failures.model.{Maybe, ServiceException, ServiceFailure}
import org.scalactic._

import scala.util.Try

object maybe extends StrictLogging {
  self =>

  def apply[T](r: => T): Maybe[T] = from(Try(r))

  def from[G](theTry: => Try[G]): Maybe[G] = {
    from("Call unexpectedly failed")(theTry)
  }

  def from[G](failureMessage: String)(theTry: => Try[G]): Maybe[G] = {
    from(mapException(Some(failureMessage)))(theTry)
  }

  def from[G](mapper: PartialFunction[Throwable, Every[ServiceFailure]])(theTry: => Try[G]): Maybe[G] = {
    log(Or.from(theTry).badMap(mapper))
  }

  def flatten[G](m: Maybe[Maybe[G]]): Maybe[G] = {
    m.flatMap(identity)
  }

  def flatten[G](theTry: => Try[Maybe[G]]): Maybe[G] = {
    flatten("Call unexpectedly failed")(theTry)
  }

  def flatten[G](failureMessage: String)(theTry: => Try[Maybe[G]]): Maybe[G] = {
    flatten(mapException(Some(failureMessage)))(theTry)
  }

  def flatten[G](mapper: PartialFunction[Throwable, Every[ServiceFailure]])(theTry: => Try[Maybe[G]]): Maybe[G] = {
    log(Or.from(theTry)
      .badMap(mapper)
      .flatMap(v => v))
  }

  def when(cond: Boolean)(m: => Maybe[Unit]): Maybe[Unit] = {
    if (cond) m else Good(())
  }

  def when[T](value: T)(m: PartialFunction[T, Maybe[Unit]]): Maybe[Unit] = {
    m.applyOrElse(value, { _: T => Good(()) })
  }

  def pure[T](r: T): Maybe[T] = Good(r)

  def void: Maybe[Unit] = Good(())

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

  implicit class TryExtensions[T](theTry: Try[T]) {
    def maybe: Maybe[T] = from(theTry)

    def maybe(failureMessage: String): Maybe[T] = from(failureMessage)(theTry)

    def maybe(mapper: PartialFunction[Throwable, Every[ServiceFailure]]): Maybe[T] = from(mapper)(theTry)
  }

  //  implicit class TryMaybeExtensions[T](theTry: Try[Maybe[T]]) {
  //    def flatten: Maybe[T] = maybe.flatten(theTry)
  //    def flatten(failureMessage: String): Maybe[T] = maybe.flatten(failureMessage)(theTry)
  //    def flatten(mapper: PartialFunction[Throwable, Every[ServiceFailure]]): Maybe[T] = maybe.flatten(mapper)(theTry)
  //  }

  implicit class MaybeExtensions[T](theMaybe: Maybe[T]) {
    def asExceptionsList: Seq[ServiceException] = {
      theMaybe match {
        case Bad(b) =>
          b.map(_.toException).toSeq
        case _ =>
          Seq()
      }
    }

    def flatten[G](implicit ev: Maybe[T] <:< Maybe[Maybe[G]]): Maybe[G] = {
      self.flatten(theMaybe)
    }

    def andThen[G](next: => Maybe[G])(implicit ev: Maybe[T] <:< Maybe[Unit]): Maybe[G] = {
      theMaybe.flatMap(_ => next)
    }

    def void: Maybe[Unit] = {
      theMaybe.map(_ => ())
    }

    def pure[G](g: => G): Maybe[G] = {
      theMaybe.map(_ => g)
    }
  }

  private def log[G](theMaybe: Maybe[G]): Maybe[G] = {
    if (logger.underlying.isDebugEnabled) {
      theMaybe.asExceptionsList.foreach {
        e =>
          logger.debug(s"Maybe has failed", e)
      }
    }
    theMaybe
  }
}
