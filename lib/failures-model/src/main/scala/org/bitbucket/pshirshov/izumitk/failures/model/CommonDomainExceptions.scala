package org.bitbucket.pshirshov.izumitk.failures.model


object CommonDomainExceptions {
  class InternalFailureException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause)

  class ForbiddenException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with DomainException

  class IllegalRequestException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with DomainException

  class NotFoundException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with DomainException

  class InvalidVersionException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with DomainException
}

