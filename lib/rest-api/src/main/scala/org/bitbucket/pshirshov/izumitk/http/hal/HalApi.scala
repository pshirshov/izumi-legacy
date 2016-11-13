package org.bitbucket.pshirshov.izumitk.http.hal

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.services.ServiceException
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import org.scalactic.{Bad, Good}

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.reflect._


@Singleton
class HalApi @Inject()
(
  representationFactory: RepresentationFactory
  , serializer: HalSerializer
) {

  protected val `application/hal+json` = MediaType.applicationWithFixedCharset(RepresentationFactory.HAL_JSON, HttpCharsets.`UTF-8`)

  protected val halContentType = ContentType(`application/hal+json`)

  def completeHal[R <: Hal : ClassTag : Manifest]
  (
    fun: => R
  ): (RequestContext) => Future[RouteResult] = {
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
              serializer.makeRepr(baseUri, dto)
            case Bad(failure) =>
              throw new ServiceException(s"Bad result: $failure") // TODO: needs to be improved
            case dto: AnyRef =>
              serializer.makeRepr(baseUri, dto)
            case other =>
              throw new UnsupportedOperationException(s"Automatic HAL serialization unsupported: $other")
          }
          handler(entity, baseUri, repr)
          repr
      }

      val body = representation.toString(RepresentationFactory.HAL_JSON)

      ctx.complete(HttpResponse(StatusCodes.OK
        , entity = HttpEntity(body).withContentType(halContentType)
      ))
  }

}