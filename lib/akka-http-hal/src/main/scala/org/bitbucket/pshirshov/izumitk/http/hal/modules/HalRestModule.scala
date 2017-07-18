package org.bitbucket.pshirshov.izumitk.http.hal.modules

import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaMultibinder
import org.bitbucket.pshirshov.izumitk.HealthChecker
import org.bitbucket.pshirshov.izumitk.akka.http.modules.AbstractRestModule
import org.bitbucket.pshirshov.izumitk.akka.http.util._
import org.bitbucket.pshirshov.izumitk.akka.http.util.cors.{CORS, DefaultCORS}
import org.bitbucket.pshirshov.izumitk.akka.http.util.serialization.{PermissiveJacksonProtocol, SerializationProtocol}
import org.bitbucket.pshirshov.izumitk.http.hal.serializer.links.{DefaultLinkExtractor, LinkExtractor}
import org.bitbucket.pshirshov.izumitk.http.hal.{DefaultHalApiPolicy, HalApiPolicy}

final class HalRestModule() extends AbstractRestModule {
  override def configure(): Unit = {
    // TODO: better place
    ScalaMultibinder.newSetBinder[HealthChecker](binder)

    bind[SerializationProtocol].to[PermissiveJacksonProtocol].in[Singleton]
    bind[HalApiPolicy].to[DefaultHalApiPolicy].in[Singleton]

    bind[RequestTransformer].to[NullRequestTransformer].in[Singleton]
    bind[LinkExtractor].to[DefaultLinkExtractor].in[Singleton]
    bind[CORS].to[DefaultCORS].in[Singleton]
  }

  @Provides
  @Singleton
  def apiPolicy(halApiPolicy: HalApiPolicy): APIPolicy = halApiPolicy
}
