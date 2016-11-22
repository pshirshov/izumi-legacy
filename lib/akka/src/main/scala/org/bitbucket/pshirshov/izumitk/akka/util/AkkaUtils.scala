package org.bitbucket.pshirshov.izumitk.akka.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  */
object AkkaUtils {
  def shutdown(implicit system: ActorSystem) = {
    Await.ready(Http().shutdownAllConnectionPools(), Duration.Inf)
    Await.ready(system.terminate(), Duration.Inf)
  }
}
