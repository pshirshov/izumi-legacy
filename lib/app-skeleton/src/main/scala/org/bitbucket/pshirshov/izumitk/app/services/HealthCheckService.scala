package org.bitbucket.pshirshov.izumitk.app.services

import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.{HealthCheck, HealthChecker}

@Singleton
class HealthCheckService @Inject()
(
  healthCheckers: Set[HealthChecker]
){
  def allHealthChecks(): Vector[HealthCheck] = healthCheckers.flatMap(_.healthCheck()).toVector
}
