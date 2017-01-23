package org.bitbucket.pshirshov.izumitk.akka.http.modules

import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule
import org.jose4j.jwk.{JsonWebKey, PublicJsonWebKey, RsaJsonWebKey}
import org.jose4j.keys.BigEndianBigInteger


final class JwtModule()
  extends ScalaModule with StrictLogging {

  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  def jwtKey(@Named("@jwt.jwt-keys.*") jwtKeys: Config): Map[String, PublicJsonWebKey] = {
    import scala.collection.JavaConverters._

    val values = jwtKeys.root().unwrapped().asScala
      .toMap.asInstanceOf[Map[String, String]]

    @scala.annotation.tailrec
    def deref(key: String): String = {
      if (key.startsWith("@")) {
        deref(values(key.substring(1)))
      } else {
        key
      }
    }

    values.map {
      case (name, keyRef) =>
        val keyString = deref(keyRef)
        val key = decodeKey(name, keyString)
        logKey(name, keyRef, keyString, key)
        name -> key
    }
  }

  private def logKey(name: String, keyRef: String, keyString: String, key: PublicJsonWebKey) = {
    if (keyString == keyRef) {
      logger.info(
        s"""Loaded security key $name: ${SecurityKeys.keyInfo(key.getPublicKey)}:${Option(key.getPrivateKey).map(SecurityKeys.keyInfo)}
           |Public Key fingerprint=${SecurityKeys.publicKeyFingerprint(key.getPublicKey)}
           |Public Key:
           |${SecurityKeys.publicKey(key.getPublicKey)}
           |Orignal key:
           |$keyString""".stripMargin)
    } else {
      logger.info(s"Loaded security key $name as reference to $keyRef")
    }
  }

  private def decodeKey(name: String, keyString: String) = {
    val key = if (keyString.contains("BEGIN")) {
      SecurityKeys.readPemKey(keyString) match {
        case k: PublicKey =>
          PublicJsonWebKey.Factory.newPublicJwk(k)
        case k: RSAPrivateCrtKey =>
          val params = Map[String, Object](
            RsaJsonWebKey.MODULUS_MEMBER_NAME -> BigEndianBigInteger.toBase64Url(k.getModulus)
            , RsaJsonWebKey.EXPONENT_MEMBER_NAME -> BigEndianBigInteger.toBase64Url(k.getPublicExponent)
            , RsaJsonWebKey.PRIVATE_EXPONENT_MEMBER_NAME -> BigEndianBigInteger.toBase64Url(k.getPrivateExponent)
            , JsonWebKey.KEY_TYPE_PARAMETER -> RsaJsonWebKey.KEY_TYPE
          )
          import scala.collection.JavaConverters._
          PublicJsonWebKey.Factory.newPublicJwk(params.asJava)
        case k =>
          throw new IllegalArgumentException(s"Unsupported key: $k")
      }

    } else {
      PublicJsonWebKey.Factory.newPublicJwk(keyString)
    }
    key
  }
}
