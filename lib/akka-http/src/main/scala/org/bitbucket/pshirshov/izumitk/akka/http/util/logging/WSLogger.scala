package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, Source}

/**
  */
object WSLogger {
  def createLoggerFlow(): Flow[Message, Message, NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val outbound = b.add(Flow[Message])
      val inbound = b.add(Flow[Message])

      val publisher = new WSPublisher()
      Source.fromPublisher(publisher).map(TextMessage.apply) ~> outbound

      /**
        *       val sink = Sink.combine(
        *       Sink.foreach[Message](t => {})
        *       , Sink.onComplete[Message](t => {}))(Broadcast[Message](_))
        *
        *       inbound ~> sink
        */
      inbound ~> Sink.onComplete {
        t =>
          WSAppender.subscribers.removeAll(publisher.subscribers)
      }

      FlowShape(inbound.in, outbound.out)
    })
  }
}
