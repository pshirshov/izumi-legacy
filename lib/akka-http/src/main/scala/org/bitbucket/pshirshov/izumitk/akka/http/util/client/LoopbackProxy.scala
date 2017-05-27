package org.bitbucket.pshirshov.izumitk.akka.http.util.client

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
/**
  *
  */
object LoopbackProxy {
  protected val marshallingTimeout: FiniteDuration = FiniteDuration.apply(5, TimeUnit.SECONDS)

  def create
  (
    system: ActorSystem
    , targetHost: String
    , targetPort: Int
  )
  (
  implicit executionContext: ExecutionContext,
  materializer: Materializer
  ): Route = {
    Route {
      context =>
        val request = context.request
        val flow = Http(system).outgoingConnection(targetHost, targetPort)
        val handler = Source.single(context.request)
          .via(flow)
          .map(_.toStrict(5.seconds))
          .runWith(Sink.head)
          .flatMap(context.complete(_))
        handler
    }
  }
}
