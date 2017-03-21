package org.bitbucket.pshirshov.izumitk.akka.http.util.cors

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives.options
import akka.http.scaladsl.server.{RequestContext, RouteResult}

import scala.concurrent.Future

/**
  */
trait CORS {
  def corsHeaders: Seq[RawHeader]

  def CORSOptions: (RequestContext) => Future[RouteResult] = {
    ctx: RequestContext =>
      ctx
        //.m(r => r.copy(headers =  ++ r.headers))
        .complete(HttpResponse(StatusCodes.OK
        , entity = HttpEntity.Empty
        , headers = corsHeaders.to[collection.immutable.Seq]
      ))
  }

  def corsOptionsRoute: server.Route = {
    options {
      CORSOptions
    }
  }
}


