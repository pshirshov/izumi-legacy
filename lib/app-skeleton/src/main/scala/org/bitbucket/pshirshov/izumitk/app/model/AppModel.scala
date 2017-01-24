package org.bitbucket.pshirshov.izumitk.app.model

import java.io.File
import java.lang.management.ManagementFactory
import java.time.ZonedDateTime

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.bitbucket.pshirshov.izumitk.{HealthCheck, HealthStatus}
import org.bitbucket.pshirshov.izumitk.app.Version
import org.bitbucket.pshirshov.izumitk.config.LoadedConfig
import org.bitbucket.pshirshov.izumitk.util.TimeUtils

import scala.concurrent.duration.Duration

/**
  */
object AppModel {

  case class BaseArguments(configFile: Option[File] = None,
                           logbackFile: Option[File] = None,
                           dump: Option[Boolean] = Option(false),
                           allowReferenceStartup: Option[Boolean] = Option(false),
                           showReference: Option[Boolean] = Option(false),
                           writeReference: Option[Boolean] = Option(false)
                          ) {}

  case class StartupConfiguration[ArgsType <: WithBaseArguments](arguments: ArgsType, config: LoadedConfig)

  trait WithBaseArguments {
    val base: BaseArguments

    def baseCopy(base: BaseArguments): this.type
  }


  case class ServiceVersion(version: String
                            , revision: String
                            , timestamp: String
                            , author: String
                           ) {
    def this() = this(Version.getVersion, Version.getRevision, Version.getBuildTimestamp, Version.getBuildUser)
  }

  private final val serviceVersion = new ServiceVersion()

  private def getUptime: Long = ManagementFactory.getRuntimeMXBean.getUptime

  @JsonIgnoreProperties(ignoreUnknown = true)
  case class ServiceInfo(
                          health: Vector[HealthCheck]
                          , version: ServiceVersion = serviceVersion
                          , timestamp: ZonedDateTime = TimeUtils.utcNow
                          , uptime: String = Duration.apply(getUptime, scala.concurrent.duration.MILLISECONDS).toSeconds.toString
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
