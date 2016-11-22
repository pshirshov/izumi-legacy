package org.bitbucket.pshirshov.izumitk.failures.model

/**
  */
class ServiceException(override val message: String, cause: Option[Throwable] = None)
  extends scala.RuntimeException(message, cause.orNull)
      with ServiceFailure
