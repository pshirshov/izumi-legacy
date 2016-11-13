package org.bitbucket.pshirshov.izumitk.http.util

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, Source}
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.reactivestreams.{Publisher, Subscriber, Subscription}


class WSAppender[T] extends AppenderBase[T] {
  protected val layout = new PatternLayout()

  def setPattern(s: String): Unit = {
    layout.setContext(context)
    layout.setPattern(s)
    layout.start()
  }

  override def append(eventObject: T): Unit = {
    import scala.collection.JavaConversions._

    eventObject match {
      case le: ILoggingEvent =>
        val msg = layout.doLayout(le)

        WSAppender.subscribers.foreach {
          s =>
            try {
              s.onNext(msg)
            } catch {
              case f: Throwable =>
                f.printStackTrace()
            }
        }
      case _ =>
    }
  }
}

object WSAppender {
  val subscribers = new java.util.concurrent.ConcurrentLinkedQueue[Subscriber[String]]()
}

class WSPublisher extends Publisher[String] {
  val subscribers = new java.util.concurrent.ConcurrentLinkedQueue[Subscriber[String]]()

  override def subscribe(s: Subscriber[_ >: String]): Unit = {
    val safeS = s.asInstanceOf[Subscriber[String]]

    subscribers.add(safeS)
    WSAppender.subscribers.add(safeS)

    s.onSubscribe(new Subscription {
      override def cancel(): Unit = {}
      override def request(n: Long): Unit = {}
    })
  }
}

object WSLogger {
  def createLoggerFlow() = {
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