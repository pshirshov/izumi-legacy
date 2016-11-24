package org.bitbucket.pshirshov.izumitk.failures.model

/**
  */
trait ServiceFailure {
  def message: String
  def toException: ServiceException
}


