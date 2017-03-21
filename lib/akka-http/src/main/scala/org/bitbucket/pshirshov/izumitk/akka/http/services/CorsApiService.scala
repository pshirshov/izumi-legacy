package org.bitbucket.pshirshov.izumitk.akka.http.services

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.akka.http.util.cors.CORS


trait CorsApiService
  extends HttpService
    with StrictLogging {

  protected def cors: CORS

  abstract override val routes: server.Route = cors.corsOptionsRoute ~ super.routes
}
