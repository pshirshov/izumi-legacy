package org.bitbucket.pshirshov.izumitk.http.jwt

import com.typesafe.scalalogging.StrictLogging
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jws.{AlgorithmIdentifiers, JsonWebSignature}
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.JwtConsumerBuilder

import scala.util.Try


class NoJwtTokenException extends RuntimeException

/**
  */
trait Jwt extends StrictLogging {
  protected val keys: Map[String, PublicJsonWebKey]

  def createJwt(keyId: String, payload: String): String = {
    val jws = new JsonWebSignature()
    jws.setPayload(payload)
    jws.setKey(keys(keyId).getPrivateKey)
    jws.setKeyIdHeaderValue(keys(keyId).getKeyId)
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256)
    jws.getCompactSerialization
  }

  def readJwt(keyId: String, token: String): Try[JwtClaims] = {
    val consumerBuilder = createConsumerBuilder.setVerificationKey(keys(keyId).getKey)
    Try(consumerBuilder.build().processToClaims(token))
  }

  // TODO: improve this
  def createConsumerBuilder = {
    new JwtConsumerBuilder().setRequireExpirationTime()
      .setAllowedClockSkewInSeconds(30)
      .setRequireSubject()
      .setExpectedIssuer("IdentityService")
      .setExpectedAudience("*")
  }
}

