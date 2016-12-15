package org.bitbucket.pshirshov.izumitk.akka.http.util.cors

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import com.google.inject.name.Named

import scala.concurrent.Future

/**
  */
trait CORS {
  val corsHeaders: Seq[RawHeader]

  def CORSOptions: (RequestContext) => Future[RouteResult] = {
    ctx: RequestContext =>
      ctx
        //.m(r => r.copy(headers =  ++ r.headers))
        .complete(HttpResponse(StatusCodes.OK
        , entity = HttpEntity.Empty
        , headers = corsHeaders.to[collection.immutable.Seq]
      ))
  }
}

import com.google.inject.{Inject, Singleton}

@Singleton
class DefaultCORS @Inject()
(
  @Named("headers.cors") override val corsHeaders: Seq[RawHeader]
) extends CORS {

}
