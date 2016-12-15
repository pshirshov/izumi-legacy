package org.bitbucket.pshirshov.izumitk.http.hal

import akka.http.scaladsl.model.MediaType.WithFixedCharset
import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, RequestContext, RouteResult}
import com.codahale.metrics.MetricRegistry
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.theoryinpractise.halbuilder.api.{Representation, RepresentationFactory}
import org.bitbucket.pshirshov.izumitk.akka.http.util.cors.CORS
import org.bitbucket.pshirshov.izumitk.akka.http.util.serialization.SerializationProtocol
import org.bitbucket.pshirshov.izumitk.akka.http.util.{APIPolicy, MetricDirectives}
import org.bitbucket.pshirshov.izumitk.failures.model.{CommonDomainExceptions, DomainException, ServiceException, ServiceFailure}
import org.bitbucket.pshirshov.izumitk.failures.services.{FailureRecord, FailureRepository}
import org.bitbucket.pshirshov.izumitk.hal.HalResource
import org.scalactic.{Bad, Every, Good}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.reflect._
import scala.util.control.NonFatal


trait HalApiPolicy extends APIPolicy {
  def completeHal[R <: Hal : ClassTag : Manifest](endpointName: String)(fun: => R): (RequestContext) => Future[RouteResult]
}

@HalResource
case class HalFailure(failureId: String, failureType: String, failureMessage: String)

@Singleton
class DefaultHalApiPolicy @Inject()
(
  representationFactory: RepresentationFactory
  , serializer: HalSerializerImpl
  , failureRepository: FailureRepository
  , cors: CORS
  , @Named("app.id") override protected val productId: String
  , override val protocol: SerializationProtocol
  , override protected val metrics: MetricRegistry
  , override protected implicit val executionContext: ExecutionContext
) extends HalApiPolicy
  with MetricDirectives {

  val `application/hal+json`: WithFixedCharset = MediaType.applicationWithFixedCharset(RepresentationFactory.HAL_JSON.split("/").last, HttpCharsets.`UTF-8`)

  val halContentType = ContentType(`application/hal+json`)

  override def completeHal[R <: Hal : ClassTag : Manifest](endpointName: String)(fun: => R): (RequestContext) => Future[RouteResult] = metered(endpointName) {
    ctx: RequestContext =>
      try {
        val body = serializeEntity(fun, ctx).toString(RepresentationFactory.HAL_JSON)

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
        case r =>
          complete((Forbidden, s"REJECTED: $r"))
      }
      .result()
  }

  override def exceptionHandler(): ExceptionHandler = {
    ExceptionHandler {
      case NonFatal(e) => ctx =>
        completeFatalException(e, ctx, "unhandledInternal")
    }
  }

  protected def serializeEntity[R <: Hal : ClassTag : Manifest](value: R, ctx: RequestContext): Representation = {
    import Hal._
    val baseUri = Href.make(Option(ctx.request))

    value match {
      case Repr(r) =>
        r
      case WithConverter(entity, converter) =>
        converter(entity, baseUri)
      case Entity(entity) =>
        entity.hal(representationFactory, baseUri)
      case Auto(entity, handler) =>
        val repr = entity match {
          case Good(dto: AnyRef) =>
            serializer.makeRepr(baseUri, dto, handler)

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
            serializer.makeRepr(baseUri, dto, handler)
          case other =>
            throw new UnsupportedOperationException(s"Automatic HAL serialization unsupported: $other")
        }
        repr
    }
  }

  protected def completeFatalException(e: Throwable, ctx: RequestContext, exceptionKind: String): Future[RouteResult] = {
    logger.error(s"Critical failure while handling request:\n${ctx.request}")

    val failureId = failureRepository.recordFailure(FailureRecord(e))
    val fdata = HalFailure(failureId, exceptionKind, e.getMessage)

    val baseUri = Href.make(Option(ctx.request))
    val body = serializer.makeRepr(baseUri, fdata, _ => {}).toString(RepresentationFactory.HAL_JSON)

    ctx.complete(HttpResponse(InternalServerError
      , entity = HttpEntity(body).withContentType(halContentType)
    ))
  }

}
