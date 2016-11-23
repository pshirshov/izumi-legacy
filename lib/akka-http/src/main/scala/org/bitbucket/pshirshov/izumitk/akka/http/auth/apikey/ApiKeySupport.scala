package org.bitbucket.pshirshov.izumitk.akka.http.auth.apikey

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.{Authorization, GenericHttpCredentials}


trait ApiKeySupport {
  protected def extractKey(scheme: String): HttpHeader => Option[String] = {
    case Authorization(c: GenericHttpCredentials) if c.scheme.toLowerCase == scheme.toLowerCase() =>
      c.params.get("")

    case _ =>
      None
  }
}
