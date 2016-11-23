package org.bitbucket.pshirshov.izumitk.akka.http.auth.jwt

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jws.{AlgorithmIdentifiers, JsonWebSignature}
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.JwtConsumerBuilder

import scala.util.Try

/**
  */
trait JwtSupport extends StrictLogging {
  protected def getKey(keyId: String): PublicJsonWebKey

  protected def jwtMapper: JacksonMapper

  protected def createJwt(keyId: String, payload: String): String = {
    val jws = new JsonWebSignature()
    jws.setPayload(payload)

    val key = getKey(keyId)
    jws.setKey(key.getPrivateKey)
    jws.setKeyIdHeaderValue(key.getKeyId)
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256)

    jws.getCompactSerialization
  }

  protected def createJwt(keyId: String, payload: JsonNode): String = {
    val jws = new JsonWebSignature()
    jws.setPayload(jwtMapper.writeValueAsString(payload))

    val key = getKey(keyId)
    jws.setKey(key.getPrivateKey)
    jws.setKeyIdHeaderValue(key.getKeyId)
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256)

    jws.getCompactSerialization
  }

  protected def readJwt(keyId: String, token: String): Try[JwtClaims] = {
    val consumerBuilder = createConsumerBuilder.setVerificationKey(getKey(keyId).getKey)
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
