package org.bitbucket.pshirshov.izumitk.app.model

import java.time.ZonedDateTime

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.bitbucket.pshirshov.izumitk.{HealthCheck, HealthStatus}
import org.bitbucket.pshirshov.izumitk.app.Version
import org.bitbucket.pshirshov.izumitk.util.TimeUtils

/**
  */
object AppModel {

  case class ServiceVersion(version: String
                            , revision: String
                            , timestamp: String
                            , author: String
                           ) {
    def this() = this(Version.getVersion, Version.getRevision, Version.getBuildTimestamp, Version.getBuildUser)
  }

  private final val serviceVersion = new ServiceVersion()

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class ServiceInfo ( health: Vector[HealthCheck]
                        , version: ServiceVersion = serviceVersion
                        , timestamp: ZonedDateTime = TimeUtils.utcNow
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
}
