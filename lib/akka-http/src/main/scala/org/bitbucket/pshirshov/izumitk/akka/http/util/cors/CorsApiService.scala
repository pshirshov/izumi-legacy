package org.bitbucket.pshirshov.izumitk.akka.http.util.cors

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.akka.http.services.HttpService
import org.bitbucket.pshirshov.izumitk.akka.http.util.logging.HttpDebug
import org.bitbucket.pshirshov.izumitk.akka.http.util.MetricDirectives


trait CorsApiService
  extends HttpService
    with MetricDirectives
    with StrictLogging {

  protected val httpDebug: HttpDebug
  protected val cors: CORS

  import httpDebug._

  final override val routes: server.Route = withDebug {
    options {
      cors.CORSOptions
    } ~
      timerDirective {
        serviceRoutes
      }
  }

  protected def serviceRoutes: server.Route
}
