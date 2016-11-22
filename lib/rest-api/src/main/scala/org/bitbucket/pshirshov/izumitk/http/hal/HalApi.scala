package org.bitbucket.pshirshov.izumitk.http.hal

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import com.codahale.metrics.MetricRegistry
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.services.ServiceException
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import org.bitbucket.pshirshov.izumitk.http.util.MetricDirectives
import org.scalactic.{Bad, Good}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.reflect._


@Singleton
class HalApi @Inject()
(
  representationFactory: RepresentationFactory
  , serializer: HalSerializer
  , @Named("app.id") override protected val productId: String
  , override protected val metrics: MetricRegistry
  , override protected implicit val executionContext: ExecutionContext
) extends MetricDirectives {

  protected val `application/hal+json` = MediaType.applicationWithFixedCharset(RepresentationFactory.HAL_JSON.split("/").last, HttpCharsets.`UTF-8`)

  protected val halContentType = ContentType(`application/hal+json`)

  def completeHal[R <: Hal : ClassTag : Manifest](endpointName: String)(fun: => R): (RequestContext) => Future[RouteResult] = metered(endpointName) {
    ctx: RequestContext =>
      import Hal._

      val baseUri = Href.make(Option(ctx.request))
      val representation = fun match {
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
            case Bad(failure) =>
              throw new ServiceException(s"Bad result: $failure") // TODO: needs to be improved
            case dto: AnyRef =>
              serializer.makeRepr(baseUri, dto, handler)
            case other =>
              throw new UnsupportedOperationException(s"Automatic HAL serialization unsupported: $other")
          }
          repr
      }

      val body = representation.toString(RepresentationFactory.HAL_JSON)

      ctx.complete(HttpResponse(StatusCodes.OK
        , entity = HttpEntity(body).withContentType(halContentType)
      ))
  }

}
