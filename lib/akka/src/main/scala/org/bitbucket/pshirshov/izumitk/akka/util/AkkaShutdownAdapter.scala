package org.bitbucket.pshirshov.izumitk.akka.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AkkaShutdownAdapter(implicit val system: ActorSystem) extends AutoCloseable {
  override def close(): Unit = {
    Await.ready(Http().shutdownAllConnectionPools(), Duration.Inf)
    Await.ready(system.terminate(), Duration.Inf)
  }
}
