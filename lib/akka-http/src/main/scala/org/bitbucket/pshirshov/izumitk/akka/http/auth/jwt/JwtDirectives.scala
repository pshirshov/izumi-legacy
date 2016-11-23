package org.bitbucket.pshirshov.izumitk.akka.http.auth.jwt

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken, RawHeader}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives.headerValue

/**
  */
trait JwtDirectives {
  protected def jwtHeaderName: String = Authorization.name
  protected def jwtHeaderSchema: String = "Bearer"

  protected def withJwt: server.Directive1[String] = headerValue(extractJwtToken())

  protected def tokenToHeader(token: String): HttpHeader = {
    jwtHeaderName match {
      case Authorization.name =>
        Authorization(OAuth2BearerToken(token))
      case nonStadardName =>
        RawHeader(jwtHeaderName, s"$jwtHeaderSchema $token")
    }
  }

  protected def extractJwtToken(): HttpHeader => Option[String] = {
    case Authorization(c: OAuth2BearerToken) =>
      Some(c.token)

    case r: RawHeader if r.name.toLowerCase == jwtHeaderName.toLowerCase =>
      val parts = r.value.split(' ')
      if (parts.length == 2 && parts.head.toLowerCase == jwtHeaderSchema.toLowerCase) {
        Some(parts.last)
      } else {
        None
      }

    case _ =>
      None
  }
}
