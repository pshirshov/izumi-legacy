package org.bitbucket.pshirshov.izumitk.akka.http.modules

import java.io.StringReader
import java.security.{PublicKey, Security}

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMReader
import org.jose4j.jwk.PublicJsonWebKey


final class JwtModule() extends ScalaModule {
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

        name -> decodeKey(keyString)

    }
  }

  private def initBouncyCastle(): Unit = {
    if (Option(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)).isEmpty) {
      Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
    }
  }

  private def getFromString(keystr: String): PublicKey = {
    initBouncyCastle()
    val reader = new PEMReader(new StringReader(keystr))
    reader.readObject().asInstanceOf[PublicKey]
  }

  private def decodeKey(keyString: String) = {
    if (keyString.contains("BEGIN")) {
      PublicJsonWebKey.Factory.newPublicJwk(getFromString(keyString))
    } else {
      PublicJsonWebKey.Factory.newPublicJwk(keyString)
    }
  }
}
