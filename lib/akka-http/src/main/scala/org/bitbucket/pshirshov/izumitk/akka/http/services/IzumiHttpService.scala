package org.bitbucket.pshirshov.izumitk.akka.http.services

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.pathPrefix

trait IzumiHttpService {
  this: HttpService =>

  protected def prefix: Directive0 = {
    pathPrefix(defaultPrefix)
  }

  protected def defaultPrefix: String
}
