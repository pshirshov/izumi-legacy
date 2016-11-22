package org.bitbucket.pshirshov.izumitk.akka.util

import akka.actor.{ActorSystem, Terminated}
import akka.http.scaladsl.Http

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
  */
object AkkaUtils {
  def shutdown(implicit system: ActorSystem): Future[Terminated] = {
    Await.ready(Http().shutdownAllConnectionPools(), Duration.Inf)
    Await.ready(system.terminate(), Duration.Inf)
  }
}
