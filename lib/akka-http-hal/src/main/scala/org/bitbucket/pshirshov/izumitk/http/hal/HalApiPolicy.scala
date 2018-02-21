package org.bitbucket.pshirshov.izumitk.http.hal

import akka.http.scaladsl.server.{RequestContext, RouteResult}
import org.bitbucket.pshirshov.izumitk.akka.http.util.APIPolicy
import org.bitbucket.pshirshov.izumitk.http.hal.model.ToHal

import scala.concurrent.Future

trait HalApiPolicy extends APIPolicy {
  def completeHal(endpointName: String)(fun: => ToHal): (RequestContext) => Future[RouteResult]
}
