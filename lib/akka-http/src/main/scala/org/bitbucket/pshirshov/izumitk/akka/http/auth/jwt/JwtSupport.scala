package org.bitbucket.pshirshov.izumitk.akka.http.auth.jwt

import java.security.Key

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.JwtConsumerBuilder

import scala.util.Try

case class JwtKeyId(name: String)

case class KeyPair(algorithmIdentifier: String, encryption: Option[Key], verification: Key)


trait JwtSupport extends StrictLogging {
  protected def getKey(keyId: JwtKeyId): KeyPair

  protected def jwtMapper: JacksonMapper

  protected def createJwt(keyId: JwtKeyId, payload: String): String = {
    val jws = new JsonWebSignature()
    jws.setPayload(payload)
    createJwt(keyId, jws)
  }

  protected def createJwt(keyId: JwtKeyId, payload: JsonNode): String = {
    val jws = new JsonWebSignature()
    jws.setPayload(jwtMapper.writeValueAsString(payload))
    createJwt(keyId, jws)
  }

  protected def createJwt(keyId: JwtKeyId, jws: JsonWebSignature): String = {
    val key = getKey(keyId)
    key.encryption match {
      case Some(e) =>
        jws.setKey(e)
      case None =>
        throw new IllegalArgumentException(s"Encryption key not defined: $key")
    }
    jws.setKeyIdHeaderValue(keyId.name)
    jws.setAlgorithmHeaderValue(key.algorithmIdentifier)
    jws.getCompactSerialization
  }

  protected def readJwt(keyId: JwtKeyId, token: String): Try[JwtClaims] = {
    val consumerBuilder = createConsumerBuilder.setVerificationKey(getKey(keyId).verification)
    Try(consumerBuilder.build().processToClaims(token))
  }

  protected def createConsumerBuilder: JwtConsumerBuilder = {
    new JwtConsumerBuilder()
      .setRequireExpirationTime()
      .setRequireSubject()
  }

  protected def createJwtPayload(claimsHandler: (JwtClaims) => Unit): ObjectNode = {
    val claims = new JwtClaims()
    claims.setGeneratedJwtId()
    claims.setIssuedAtToNow()
    claimsHandler(claims)

    jwtMapper.readTree(claims.toJson).asInstanceOf[ObjectNode]
  }

}
