package org.bitbucket.pshirshov.izumitk.akka.http.services

import akka.http.scaladsl._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.WebSocketDirectives
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.akka.http.util.RequestTransformer


trait HttpService {
  val routes: server.Route
}

@Singleton
class HttpApiService @Inject()
(
  httpServices: scala.collection.immutable.Set[HttpService]
  , requestTransformer: RequestTransformer
) extends HttpService
{
  import Directives._

  override val routes: Route =
    mapRequestContext(requestTransformer.requestMapper) {
      httpServices
        .map(_.routes)
        .foldLeft[Route](reject) {
        case (acc, r) =>
          acc ~ r
      }
    }
}

trait WebSocketService extends WebSocketDirectives {
  this: HttpService =>
}
