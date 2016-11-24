package org.bitbucket.pshirshov.izumitk.akka.http.util

import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import org.bitbucket.pshirshov.izumitk.akka.http.util.serialization.SerializationProtocol

/**
  */
trait APIPolicy {
  val protocol: SerializationProtocol

  def rejectionHandler(): RejectionHandler

  def exceptionHandler(): ExceptionHandler
}
