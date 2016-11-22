package org.bitbucket.pshirshov.izumitk.akka.http.modules

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule


final class HttpModule() extends ScalaModule {
  override def configure(): Unit = {}

  @Provides
  @Singleton
  def defaultHttpLogHandler: HttpDebugLogHandler = new NoopHttpDebugLogHandler()

  @Provides
  @Singleton
  def flow(
            service: HttpService
          )
          (
            implicit system: ActorSystem
            , materializer: Materializer
            , exceptionHandler: ExceptionHandler
            , rejectionHandler: RejectionHandler
          ): Flow[HttpRequest, HttpResponse, Any] = {
    import akka.http.scaladsl.server.RouteResult
    RouteResult.route2HandlerFlow(service.routes)
  }
}
