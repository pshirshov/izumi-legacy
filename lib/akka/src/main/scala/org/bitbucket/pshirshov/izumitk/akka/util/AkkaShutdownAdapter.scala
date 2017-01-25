package org.bitbucket.pshirshov.izumitk.akka.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.google.inject.{Inject, Singleton}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class AkkaShutdownAdapter @Inject()(system: ActorSystem) extends AutoCloseable {
  override def close(): Unit = {
    Await.ready(Http().shutdownAllConnectionPools(), Duration.Inf)
    Await.ready(system.terminate(), Duration.Inf)
  }
}
