package org.bitbucket.pshirshov.izumitk.http.services

import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.http.utils.TimeoutUtils

import scala.concurrent.duration.{Duration, FiniteDuration}

class TimeoutService @Inject
(
  @Named("@timeout.*") timeoutConfig: Config
)() extends StrictLogging with TimeoutUtils {

  private val defaultTimeout = timeoutConfig.getString("default-timeout")

  def get[T](clazz: Class[T], id: String = "default"): FiniteDuration =
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
