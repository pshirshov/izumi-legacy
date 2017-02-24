package org.bitbucket.pshirshov.izumitk.app.model

import java.lang.management.ManagementFactory
import java.time.ZonedDateTime

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.bitbucket.pshirshov.izumitk.util.types.TimeUtils
import org.bitbucket.pshirshov.izumitk.{HealthCheck, HealthStatus}

import scala.concurrent.duration.Duration

object ServiceInfo {
  private final val serviceVersion = new ServiceVersion()
  private def getUptime: Long = ManagementFactory.getRuntimeMXBean.getUptime
  private def getUptimeDuration: Long = Duration.apply(getUptime, scala.concurrent.duration.MILLISECONDS).toSeconds
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class ServiceInfo(
                        health: Vector[HealthCheck]
                        , version: ServiceVersion = ServiceInfo.serviceVersion
                        , timestamp: ZonedDateTime = TimeUtils.utcNow
                        , uptime: String = ServiceInfo.getUptimeDuration.toString
                      ) {
  def getStatus: HealthStatus = {
    if (health.forall(_.status == HealthStatus.OK)) {
      HealthStatus.OK
    } else if (health.exists(_.status == HealthStatus.DEFUNCT)) {
      HealthStatus.DEFUNCT
    } else {
      HealthStatus.UNKNOWN
    }
  }
}
