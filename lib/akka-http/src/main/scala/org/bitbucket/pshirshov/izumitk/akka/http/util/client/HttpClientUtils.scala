package org.bitbucket.pshirshov.izumitk.akka.http.util.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

import scala.util.Try

object HttpClientUtils {
  def createHttpPool[IdType](uri: Uri)
  (
    implicit system: ActorSystem
    , materializer: ActorMaterializer
  ): Flow[(HttpRequest, IdType), (Try[HttpResponse], IdType), HostConnectionPool] = {
    val host = uri.authority.host.address()
    val port = uri.effectivePort
    if (uri.scheme == "https") {
      Http().cachedHostConnectionPoolHttps[IdType](host, port)
    } else {
      Http().cachedHostConnectionPool[IdType](host, port)
    }
  }
}
