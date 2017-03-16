package org.bitbucket.pshirshov.izumitk.akka.http.util.cors

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.akka.http.services.HttpService


trait CorsApiService
  extends HttpService
    with StrictLogging {

  abstract override val routes: server.Route =
    options {
      cors.CORSOptions
    } ~ super.routes

  protected def cors: CORS
}
