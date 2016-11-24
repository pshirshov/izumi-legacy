package org.bitbucket.pshirshov.izumitk.http.hal.modules

import com.google.inject.Singleton
import net.codingwell.scalaguice.ScalaMultibinder
import org.bitbucket.pshirshov.izumitk.HealthChecker
import org.bitbucket.pshirshov.izumitk.akka.http.modules.AbstractRestModule
import org.bitbucket.pshirshov.izumitk.akka.http.util.{DefaultCORS, NullRequestTransformer, RequestTransformer, CORS}
import org.bitbucket.pshirshov.izumitk.akka.http.util.serialization.{PermissiveJacksonProtocol, SerializationProtocol}
import org.bitbucket.pshirshov.izumitk.http.hal.{DefaultHalApiPolicy, HalApiPolicy}

final class HalRestModule() extends AbstractRestModule {
  override def configure(): Unit = {
    // TODO: better place
    ScalaMultibinder.newSetBinder[HealthChecker](binder)

    bind[SerializationProtocol].to[PermissiveJacksonProtocol].in[Singleton]
    bind[HalApiPolicy].to[DefaultHalApiPolicy].in[Singleton]
    bind[RequestTransformer].to[NullRequestTransformer].in[Singleton]
    bind[CORS].to[DefaultCORS].in[Singleton]
  }
}
