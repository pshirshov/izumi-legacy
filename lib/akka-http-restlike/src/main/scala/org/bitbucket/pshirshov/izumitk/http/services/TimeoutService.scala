package org.bitbucket.pshirshov.izumitk.http.services

import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.Duration

class TimeoutService @Inject
(
  @Named("@timeout.*") timeoutConfig: Config
)() extends StrictLogging {

  private val defaultTimeout = timeoutConfig.getString("default-timeout")

  def get[T](clazz: Class[T], id: String = "default"): Duration =
    Duration(
      try {
        timeoutConfig.getString(s"${clazz.getName}.$id")
      } catch {
        case e: com.typesafe.config.ConfigException =>
          logger.error(e.getMessage)
          logger.info("Setting default timeout value")
          defaultTimeout
      }
    )
}
