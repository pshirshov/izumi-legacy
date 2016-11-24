package org.bitbucket.pshirshov.izumitk.akka.http.modules

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.akka.http.util.APIPolicy

/**
  */
abstract class AbstractRestModule() extends ScalaModule {
  @Provides
  @Singleton
  def exceptionHandler(policy: APIPolicy): ExceptionHandler = policy.exceptionHandler()

  @Provides
  @Singleton
  def rejectionHandler(policy: APIPolicy): RejectionHandler = policy.rejectionHandler()

  @Provides
  @Singleton
  @Named("headers.cors")
  def corsHeaders(@Named("@rest.cors.headers.*") config: Config): Seq[RawHeader] = {
    import scala.collection.JavaConverters._

    config.entrySet().asScala.map {
      entry =>
        RawHeader(entry.getKey, entry.getValue.unwrapped().asInstanceOf[String])
    }.toSeq
  }
}
