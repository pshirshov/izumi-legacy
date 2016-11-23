package org.bitbucket.pshirshov.izumitk.failures.model.http

import org.bitbucket.pshirshov.izumitk.failures.model.{ControlException, ServiceException}


object HttpExceptions {

  class InternalFailureException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause)

  class ForbiddenException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with ControlException

  class IllegalRequestException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with ControlException

  class NotFoundException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with ControlException

  class InvalidVersionException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with ControlException
}

