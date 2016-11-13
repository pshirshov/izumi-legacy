package org.bitbucket.pshirshov.izumitk.services

/**
  */
trait ServiceFailure {
  val message: String
}

class ServiceException(override val message: String, cause: Option[Throwable] = None)
  extends scala.RuntimeException(message, cause.orNull)
    with ServiceFailure
