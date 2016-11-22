package org.bitbucket.pshirshov.izumitk.http.modules

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import org.bitbucket.pshirshov.izumitk.HealthChecker
import org.bitbucket.pshirshov.izumitk.http.rest._
import com.typesafe.config.Config
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}

abstract class AbstractRestModule() extends ScalaModule {
  @Provides
  @Singleton
  def exceptionHandler(policy: JsonAPIPolicy): ExceptionHandler = policy.exceptionHandler()

  @Provides
  @Singleton
  def rejectionHandler(policy: JsonAPIPolicy): RejectionHandler = policy.rejectionHandler()

  @Provides
  @Singleton
  @Named("headers.cors")
  def corsHeaders(@Named("@rest.cors.headers.*") config: Config): Seq[RawHeader] = {
    import scala.collection.JavaConversions._

    config.entrySet().map {
      entry =>
        RawHeader(entry.getKey, entry.getValue.unwrapped().asInstanceOf[String])
    }.toSeq
  }
}


class RestModule() extends AbstractRestModule {
  override def configure(): Unit = {
    // TODO: better place
    ScalaMultibinder.newSetBinder[HealthChecker](binder)

    bind[SerializationProtocol].to[JacksonProtocol].in[Singleton]
    bind[JsonAPIPolicy].to[DefaultJsonAPIPolicy].in[Singleton]
    bind[RequestTransformer].to[DefaultJsonAPIPolicy].in[Singleton]
  }
}
