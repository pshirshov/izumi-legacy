package org.bitbucket.pshirshov.izumitk.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success


@Singleton
final class AkkaHttpApp @Inject()
(
  @Named("@http.interface") host: String
  , @Named("@http.port") port: Int
  , flow: Flow[HttpRequest, HttpResponse, Any]
)
(
  implicit actorSystem: ActorSystem
  , materializer: Materializer
  , executionContext: ExecutionContext
)
  extends Application[ServerBinding] with StrictLogging {

  override def run(): Future[ServerBinding] = {
    val future = Http().bindAndHandle(flow, host, port)
    future.onComplete {
      case Success(_) =>
        logger.info(s"Akka HTTP is available on $host:$port")

      case _ =>

    }
    future
  }

  override def shutdown(): Unit = {
  }
}
