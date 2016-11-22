package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import org.reactivestreams.{Publisher, Subscriber, Subscription}

/**
  */
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
