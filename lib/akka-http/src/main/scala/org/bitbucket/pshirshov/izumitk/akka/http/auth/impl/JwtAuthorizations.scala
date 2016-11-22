package org.bitbucket.pshirshov.izumitk.akka.http.auth.impl

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken, RawHeader}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.akka.http.util.jwt.Jwt
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import org.jose4j.json.JsonUtil
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jwt.JwtClaims
import scala.collection.JavaConverters._

import scala.util.Try


@Singleton
class JwtAuthorizations @Inject() (
                         @Named("standardMapper") jwtMapper: JacksonMapper
                         , @Named("@rest.jwt-response-header") jwtHeaderName: String
                         , @Named("@auth.jwt_expiration_minutes") tokenExpirationMinutes: Int
                         , protected val keys: Map[String, PublicJsonWebKey]
                       ) extends Jwt {
  def withJwt: server.Directive1[String] = headerValue(extractJwtToken())

  def tokenToHeader(token: String): Option[RawHeader] = {
    //Some(Authorization(GenericHttpCredentials("Jwt", Map("" -> token))))
    Some(RawHeader(jwtHeaderName, s"Bearer $token"))
  }

  def createJwtToken(keyId: String, subject: String, modifier: (ObjectNode, ObjectMapper) => JsonNode): String = {
    val claims = new JwtClaims()
    claims.setGeneratedJwtId()
    claims.setIssuedAtToNow()

    // todo: add content into claims
    claims.setIssuer("IdentityService")
    claims.setAudience("*")
    //claims.setAudience(requestedBy.asString)
    claims.setExpirationTimeMinutesInTheFuture(tokenExpirationMinutes)
    claims.setNotBeforeMinutesInThePast(2)
    claims.setSubject(subject)
    claims.setStringListClaim("roles", List.empty[String].asJava)

    val tree = jwtMapper.readTree(claims.toJson).asInstanceOf[ObjectNode]
    val token = jwtMapper.writeValueAsString(modifier(tree, jwtMapper))
    createJwt(keyId, token)
  }

  def readJwtField[T:Manifest](keyId: String, token: String, field: String): Try[T] = {
    readJwt(keyId, token) map {
      claims =>
        val payload = JsonUtil.toJson(claims.getClaimValue(field).asInstanceOf[java.util.Map[String, _]])
        jwtMapper.readValue[T](payload)
    }
  }

  def extractJwtToken(): HttpHeader => Option[String] = {
    case Authorization(c: OAuth2BearerToken) =>
      Some(c.token)

    case r: RawHeader if r.name.toLowerCase == jwtHeaderName.toLowerCase =>
      val parts = r.value.split(' ')
      if (parts.length == 2 && parts.head.toLowerCase == "bearer") {
        Some(parts.last)
      } else {
        None
      }

    case _ =>
      None
  }


}
