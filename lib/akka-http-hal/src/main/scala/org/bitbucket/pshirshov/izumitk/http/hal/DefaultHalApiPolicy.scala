package org.bitbucket.pshirshov.izumitk.http.hal

import akka.http.scaladsl.model.MediaType.WithFixedCharset
import akka.http.scaladsl.model.StatusCodes.ClientError
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server._
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.theoryinpractise.halbuilder5.{ResourceRepresentation, Support}
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.akka.http.services.HttpServiceConfiguration
import org.bitbucket.pshirshov.izumitk.akka.http.util.MetricDirectives
import org.bitbucket.pshirshov.izumitk.akka.http.util.cors.CORS
import org.bitbucket.pshirshov.izumitk.akka.http.util.serialization.SerializationProtocol
import org.bitbucket.pshirshov.izumitk.failures.model.{CommonDomainExceptions, DomainException, ServiceException, ServiceFailure}
import org.bitbucket.pshirshov.izumitk.failures.services.{FailureRecord, FailureRepository}
import org.bitbucket.pshirshov.izumitk.http.hal.model.{HalExceptionContext, HalFailure, JwtRejection, ToHal}
import org.bitbucket.pshirshov.izumitk.http.hal.serializer.{HalSerializer, JacksonHalSerializer}
import org.bitbucket.pshirshov.izumitk.util.types.ExceptionUtils
import org.scalactic.{Bad, Every, Good}

import scala.concurrent.Future
import scala.util.control.NonFatal


@Singleton
class DefaultHalApiPolicy @Inject()
(
  jacksonHalSerializer: JacksonHalSerializer
  , serializer: HalSerializer
  , failureRepository: FailureRepository
  , cors: CORS
  , override val protocol: SerializationProtocol
  , override protected val httpServiceConfiguration: HttpServiceConfiguration
  , @Named("@http.debug.exceptions") protected val debugStacktraces: Boolean
) extends HalApiPolicy
  with MetricDirectives
  with StrictLogging {

  val `application/hal+json`: WithFixedCharset = MediaType.applicationWithFixedCharset(Support.HAL_JSON.split("/").last, HttpCharsets.`UTF-8`)

  val halContentType = ContentType(`application/hal+json`)

  override def completeHal(endpointName: String)(fun: => ToHal): (RequestContext) => Future[RouteResult] = metered(endpointName) {
    ctx: RequestContext =>
      try {
        val body = jacksonHalSerializer.writeValueAsString(serializeEntity(fun, ctx.request))

        ctx.complete(HttpResponse(StatusCodes.OK
          , entity = HttpEntity(body).withContentType(halContentType)
          , headers = cors.corsHeaders.to[collection.immutable.Seq]
        ))

      } catch {
        case _: CommonDomainExceptions.NotFoundException =>
          ctx.complete(HttpResponse(StatusCodes.NotFound))
        case _: CommonDomainExceptions.ForbiddenException =>
          ctx.complete(HttpResponse(StatusCodes.Forbidden))
        case _: CommonDomainExceptions.IllegalRequestException =>
          ctx.complete(HttpResponse(StatusCodes.BadRequest))
        case other: Throwable =>
          completeFatalException(other, ctx, "handledInternal")
      }
  }

  override def rejectionHandler(): RejectionHandler = {
    RejectionHandler.newBuilder()
      .handle {
        case r: JwtRejection =>
          val resp = protocol.protocolMapper.getNodeFactory.objectNode()
          resp.put("error", "invalid_token")
          resp.put("error_description", s"Access token expired: ${r.token}")

          respond(StatusCodes.Unauthorized, resp)

        case r: Rejection =>
          val resp = protocol.protocolMapper.getNodeFactory.objectNode()
          resp.put("error", "request_rejected")
          resp.put("error_description", r.toString)

          respond(StatusCodes.NotFound, resp)
      }
      .result()
  }

  private def respond(code: ClientError, resp: ObjectNode) = {
    complete(HttpResponse(code
      , entity = HttpEntity(protocol.protocolMapper.writeValueAsString(resp)).withContentType(halContentType)
    ))
  }

  override def exceptionHandler(): ExceptionHandler = {
    ExceptionHandler {
      case NonFatal(e) => ctx =>
        completeFatalException(e, ctx, "unhandledInternal")
    }
  }

  protected def serializeEntity(value: ToHal, ctx: HttpRequest): ResourceRepresentation[ObjectNode] = {
    import ToHal._

    value match {
      case Repr(r) =>
        r

      case Auto(entity, hc) =>
        val repr = entity match {
          case Good(dto: AnyRef) =>
            serializer.makeRepr(dto, hc, ctx)

          case Bad(r: Every[ServiceFailure]) =>
            val (controlExceptions, exceptions) = r.toList.map(_.toException)
              .partition(_.isInstanceOf[DomainException])

            controlExceptions match {
              case head :: Nil =>
                throw head
              case head :: tail =>
                logger.warn(s"Too much control exceptions, skipping them: $tail")
                throw head
              case _ =>
                logger.warn(s"$controlExceptions")
            }

            exceptions match {
              case head :: Nil =>
                throw head
              case head :: tail =>
                logger.warn(s"Too much exceptions, skipping them: $tail")
                throw head
              case _ =>
            }

            throw new ServiceException(s"Deadly unexpected failures list: $r")

          case Bad(other) =>
            throw new ServiceException(s"Deadly unexpected result: $other")

          case dto: AnyRef =>
            serializer.makeRepr(dto, hc, ctx)

          case other =>
            throw new UnsupportedOperationException(s"Automatic HAL serialization unsupported: $other")
        }
        repr
    }
  }

  protected def completeFatalException(e: Throwable, ctx: RequestContext, exceptionKind: String): Future[RouteResult] = {
    logger.error(s"Critical failure while handling request:\n${ctx.request}")

    val failureId = failureRepository.recordFailure(FailureRecord(e))
    val stacktrace = if (debugStacktraces) {
      Some(ExceptionUtils.format(e))
    } else {
      None
    }

    val fdata = HalFailure(
      failureId
      , exceptionKind
      , e.getMessage
      , stacktrace
    )

    val body = jacksonHalSerializer.writeValueAsString(
      serializer.makeRepr(fdata, HalExceptionContext(e), ctx.request)
    )

    ctx.complete(HttpResponse(StatusCodes.InternalServerError
      , entity = HttpEntity(body).withContentType(halContentType)
    ))
  }

}
