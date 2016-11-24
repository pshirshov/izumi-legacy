package org.bitbucket.pshirshov.izumitk.failures.model

/**
  */
class ServiceException(
                        override val message: String
                        , cause: Option[Throwable] = None
                        , enableSuppression: Boolean = true
                        , writableStackTrace: Boolean = true
                      )
  extends scala.RuntimeException(message, cause.orNull, enableSuppression, writableStackTrace)
      with ServiceFailure {
  override def toException: ServiceException = this
}

