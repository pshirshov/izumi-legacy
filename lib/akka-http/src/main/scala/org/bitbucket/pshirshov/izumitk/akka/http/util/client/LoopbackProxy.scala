package org.bitbucket.pshirshov.izumitk.akka.http.util.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.ExecutionContext

/**
  *
  */
object LoopbackProxy {
  def create
  (
    system: ActorSystem
    , targetHost: String
    , targetPort: Int
  )
  (
  implicit executionContext: ExecutionContext,
  materializer: Materializer
  ) = {
    Route {
      context =>
        val request = context.request
        val flow = Http(system).outgoingConnection(targetHost, targetPort)
        val handler = Source.single(context.request)
          .via(flow)
          .runWith(Sink.head)
          .flatMap(context.complete(_))
        handler
    }
  }
}
