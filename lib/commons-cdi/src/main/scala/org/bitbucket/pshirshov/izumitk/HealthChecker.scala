package org.bitbucket.pshirshov.izumitk


case class HealthCheck(name: String, status: HealthStatus)


trait HealthChecker {
  def healthCheck(): Vector[HealthCheck]
}
