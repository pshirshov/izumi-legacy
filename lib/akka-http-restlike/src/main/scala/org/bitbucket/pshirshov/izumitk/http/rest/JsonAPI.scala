package org.bitbucket.pshirshov.izumitk.http.rest

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.akka.http.util.MetricDirectives
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import org.bitbucket.pshirshov.izumitk.services._
import org.scalactic._

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.control.NonFatal


object JsonAPI {

  class InternalFailureException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause)

  trait ControlException
    extends RuntimeException with ServiceFailure

  class ForbiddenException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with ControlException

  class IllegalRequestException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with ControlException

  class NotFoundException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with ControlException

  class InvalidVersionException(message: String, cause: Option[Throwable] = None)
    extends ServiceException(message, cause) with ControlException

  case class Result[R: Manifest](data: Or[R, Every[ServiceFailure]]
                                 , headers: R => Traversable[HttpHeader] = empty
                                 , resultTransformer: Option[R => AnyRef] = None
                                 , endpointName: String
                                )

  private def empty[R]: R => Seq[HttpHeader] = {
    _ =>
      Seq()
  }

  object Codes {
    final val OK = 0
    final val INTERNAL_FAILURE = 1
    final val INVALID_TOKEN = 2
    final val PERMISSION_DENIED = 3
    final val MALFORMED_REQUEST = 4
    final val NOT_FOUND = 5
    final val INVALID_PATH = 6
    final val INVALID_VERSION = 7
  }

  case class Response(body: Any, headers: Iterable[HttpHeader])

}

trait JsonAPI extends SerializationProtocol with StrictLogging {
  protected val apiPolicy: JsonAPIPolicy

  override val protocolMapper: JacksonMapper = apiPolicy.protocol.protocolMapper

  override implicit def entityUnmarshaler[T: Manifest]: FromEntityUnmarshaller[T] = apiPolicy.protocol.entityUnmarshaler[T]

  override implicit def entityMarshaler[T <: AnyRef]: ToEntityMarshaller[T] = apiPolicy.protocol.entityMarshaler[T]

  protected def completeJson[R <: AnyRef: Manifest](fun: => JsonAPI.Result[R]): (RequestContext) => Future[RouteResult] = {
    ctx: RequestContext =>
      val (body, headers) = completeJsonBody(fun)
      ctx.complete(Tuple3(StatusCodes.OK, headers, body))
  }


  protected def completeJsonBody[R <: AnyRef : Manifest](fun: => JsonAPI.Result[R]): (JsonNode, scala.collection.immutable.Seq[HttpHeader]) = {
    val output: JsonAPI.Result[R] = try {
      fun
    } catch {
      case NonFatal(t) =>
        JsonAPI.Result(Bad(One(new ServiceException("Server failure", Some(t)))), endpointName = "unknown")
    }

    val transformedOutput = output.data.map {
      r =>
        val transformedResponse: AnyRef = output.resultTransformer.map(f => f(r)).getOrElse(r)
        val headers = output.headers(r).toSeq :+ RawHeader(MetricDirectives.ENDPOINT_NAME_HEADER, output.endpointName)
        JsonAPI.Response(transformedResponse, headers)
    }

    apiPolicy.formatResponse(transformedOutput)
  }

}

