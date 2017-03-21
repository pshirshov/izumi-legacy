package org.bitbucket.pshirshov.izumitk.akka.http.services

import akka.http.scaladsl.server.directives.WebSocketDirectives

trait WebSocketService extends WebSocketDirectives {
  this: HttpService =>
}
