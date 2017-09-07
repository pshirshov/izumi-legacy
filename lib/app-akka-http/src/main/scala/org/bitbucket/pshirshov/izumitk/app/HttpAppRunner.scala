package org.bitbucket.pshirshov.izumitk.app

import akka.http.scaladsl.Http.ServerBinding
import com.codahale.metrics.JmxReporter
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.app.util.JMXUtils.JMXMPContext
import org.bitbucket.pshirshov.izumitk.cluster.model.NodeAddress
import org.eclipse.jetty.server.Server

import scala.concurrent.Await
import scala.concurrent.duration._


@Singleton
class HttpAppRunner[T] @Inject()
(
  protected val jettyServer: Server
  , protected val jmxmp: JMXMPContext
  , protected val application: Application[T]
  , protected val metricsReporter: JmxReporter
  , @Named("clustering.node.address") protected val localNodeAddress: NodeAddress
) extends StrictLogging {

  def start(): T = {
    logger.info(s"Node Address: $localNodeAddress")

    metricsReporter.start()

    jmxmp.jMXConnectorServer.start()
    logger.info(s"JMXMP is available on ${jmxmp.jMXConnectorServer.getAddress}")

    jettyServer.start()
    logger.info(s"Jetty is available on ${jettyServer.getURI}")

    Await.result(application.run(), 1.second)
  }

  def shutdown(binding: ServerBinding): Unit = {
    Await.ready(binding.unbind(), Duration.Inf)
    jmxmp.jMXConnectorServer.stop()
    metricsReporter.stop()
  }
}
