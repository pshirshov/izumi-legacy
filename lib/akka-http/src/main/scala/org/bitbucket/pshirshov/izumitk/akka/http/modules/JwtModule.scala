package org.bitbucket.pshirshov.izumitk.akka.http.modules

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
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

    values.map {
      case (name, key) =>
        val keyToUse = if (key.startsWith("@")) {
          values(key.substring(1))
        } else {
          key
        }

        name -> PublicJsonWebKey.Factory.newPublicJwk(keyToUse)
    }
  }
}
