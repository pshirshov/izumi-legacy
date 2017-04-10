package org.bitbucket.pshirshov.izumitk.akka.http.modules

import java.security.PublicKey
import java.security.interfaces.{RSAKey, RSAPrivateCrtKey}

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.{Config, ConfigObject}
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.akka.http.auth.jwt.{JwtKeyId, KeyPair}
import org.jose4j.jwk._
import org.jose4j.keys.BigEndianBigInteger


final class JwtModule()
  extends ScalaModule with StrictLogging {

  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  def jwtKey(@Named("@jwt.jwt-keys.*") jwtKeys: Config): Map[JwtKeyId, KeyPair] = {
    import scala.collection.JavaConverters._

    val values = jwtKeys

    @scala.annotation.tailrec
    def deref(keyConfig: Config): Config = {
      if (keyConfig.hasPath("ref")) {
        deref(values.getConfig(keyConfig.getString("ref")))
      } else {
        keyConfig
      }
    }

    values.root().entrySet().asScala.map {
      v =>
        val name = v.getKey
        val keyConfig = deref(v.getValue.asInstanceOf[ConfigObject].toConfig)
        val key = decodeKey(name, keyConfig)
        logKey(name, keyConfig, key)
        JwtKeyId(name) -> key
    }.toMap
  }

  private def logKey(name: String, keyConfig: Config, key: KeyPair): Unit = {
    if (keyConfig.hasPath("ref")) {
      logger.info(s"Loaded security key `$name` as reference to `${keyConfig.getString("ref")}`")
    } else {
      logger.info(
        s"""Loaded security key `$name`:
           |- JWT Algorithm   : ${key.algorithmIdentifier}
           |- Encryption Key  : ${key.encryption.fold("N/A")(SecurityKeys.keyInfo)}
           |- Verification Key: ${SecurityKeys.keyInfo(key.verification)}
           |""".stripMargin
      )

      key.verification match {
        case rsa: RSAKey =>
          logger.info(
            s""" Key `$name` is RSA key. Below are key details:
               | - Public Key fingerprint: ${SecurityKeys.publicKeyFingerprint(key.verification)}
               | - Public Key as PEM:
               | ${SecurityKeys.writePublicPemKey(key.verification)}
            """.stripMargin)
      }
    }
  }

  private def decodeKey(name: String, keyConfig: Config): KeyPair = {
    val keyString = keyConfig.getString("key")
    val algo = keyConfig.getString("algorithm")

    val key: JsonWebKey = if (keyString.contains("BEGIN")) {
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
          JsonWebKey.Factory.newJwk(params.asJava)
        case k =>
          throw new IllegalArgumentException(s"Unsupported key: $k")
      }
    } else {
      JsonWebKey.Factory.newJwk(keyString)
    }

    key match {
      case k: RsaJsonWebKey =>
        KeyPair(algo, Some(k.getRsaPrivateKey), k.getRsaPublicKey)
      case k: EllipticCurveJsonWebKey =>
        KeyPair(algo, Some(k.getEcPrivateKey), k.getECPublicKey)
      case k: PublicKey =>
        KeyPair(algo, None, k)
      case k: OctetSequenceJsonWebKey =>
        KeyPair(algo, Some(k.getKey), k.getKey)
      case _ =>
        throw new IllegalArgumentException(s"Unsupported key type: $key")
    }
  }
}
