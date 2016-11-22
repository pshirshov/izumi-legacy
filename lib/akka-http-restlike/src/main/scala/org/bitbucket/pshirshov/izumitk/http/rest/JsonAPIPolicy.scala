package org.bitbucket.pshirshov.izumitk.http.rest

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, RequestContext, RouteResult}
import com.fasterxml.jackson.databind.JsonNode
import org.bitbucket.pshirshov.izumitk.failures.model.ServiceFailure
import org.scalactic.{Every, Or}

import scala.concurrent.Future


trait JsonAPIPolicy {
  val protocol: SerializationProtocol

  def CORSOptions: (RequestContext) => Future[RouteResult]

  def rejectionHandler(): RejectionHandler

  def exceptionHandler(): ExceptionHandler

  def formatResponse[R: Manifest](transformedOutput: Or[JsonAPI.Response, Every[ServiceFailure]]): (JsonNode, collection.immutable.Seq[HttpHeader])
}
