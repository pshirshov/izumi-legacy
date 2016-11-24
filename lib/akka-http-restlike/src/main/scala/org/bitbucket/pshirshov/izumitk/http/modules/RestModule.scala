package org.bitbucket.pshirshov.izumitk.http.modules

import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaMultibinder
import org.bitbucket.pshirshov.izumitk.HealthChecker
import org.bitbucket.pshirshov.izumitk.akka.http.modules.AbstractRestModule
import org.bitbucket.pshirshov.izumitk.akka.http.util.serialization.{JacksonProtocol, SerializationProtocol}
import org.bitbucket.pshirshov.izumitk.akka.http.util._
import org.bitbucket.pshirshov.izumitk.http.rest._


class RestModule() extends AbstractRestModule {
  override def configure(): Unit = {
    // TODO: better place
    ScalaMultibinder.newSetBinder[HealthChecker](binder)

    bind[SerializationProtocol].to[JacksonProtocol].in[Singleton]
    bind[JsonAPIPolicy].to[DefaultJsonAPIPolicy].in[Singleton]
    bind[RequestTransformer].to[NullRequestTransformer].in[Singleton]
    bind[CORS].to[DefaultCORS].in[Singleton]
  }

  @Provides
  @Singleton
  def apiPolicy(jsonApiPolicy: JsonAPIPolicy): APIPolicy = jsonApiPolicy
}
