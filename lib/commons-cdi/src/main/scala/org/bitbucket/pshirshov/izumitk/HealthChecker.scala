package org.bitbucket.pshirshov.izumitk


case class HealthCheck(name: String, status: HealthStatus)


/**
  * A trait inteded to represent an abstract health checker, returning
  * a sequence of abstract named healthchecks. Like database connection status,
  * web server availability, etc.
  *
  * '''Warning''': you must never statically inject health checker dependencies in
  * constructor. You should inject the [[com.google.inject.Injector]] instead.
  *
  * Healthchecker must never throw and must not prevent DI context from being created.
  *
  */
trait HealthChecker {
  def healthCheck(): Vector[HealthCheck]
}
