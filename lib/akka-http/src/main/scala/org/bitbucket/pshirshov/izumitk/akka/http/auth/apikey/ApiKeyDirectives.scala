package org.bitbucket.pshirshov.izumitk.akka.http.auth.apikey

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives.headerValue

/**
  */
trait ApiKeyDirectives {
  protected def keySchemaName: String

  protected def extractApiKey(): HttpHeader => Option[String]

  protected def withApiKey: server.Directive1[String] = headerValue(extractApiKey())
}
