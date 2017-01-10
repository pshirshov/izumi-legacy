package org.bitbucket.pshirshov.izumitk.akka.http.modules

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule
import org.jose4j.jwk.PublicJsonWebKey


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
      case (name, key) =>
        val keyString = deref(key)
        name -> decodeKey(name, keyString)
    }
  }


  private def decodeKey(name: String, keyString: String) = {
    val key = if (keyString.contains("BEGIN")) {
      PublicJsonWebKey.Factory.newPublicJwk(SecurityKeys.readPemKey(keyString))
    } else {
      PublicJsonWebKey.Factory.newPublicJwk(keyString)
    }
    logger.info(s"""Loaded security key $name: ${SecurityKeys.keyInfo(key.getPublicKey)}:${Option(key.getPrivateKey).map(SecurityKeys.keyInfo)}
         |Public Key fingerprint=${SecurityKeys.keyFingerprint(key.getPublicKey)}
         |Orignal key:
         |$keyString""".stripMargin)

    key
  }
}
