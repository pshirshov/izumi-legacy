package org.bitbucket.pshirshov.izumitk.cassandra

import com.datastax.driver.core.Session
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.{HealthCheck, HealthChecker, HealthStatus}

import scala.util.{Failure, Success, Try}


@Singleton
class CassandraHealthChecker @Inject()
(
  protected val session: Session
) extends HealthChecker {
  override def healthCheck(): Vector[HealthCheck] = {

    val cassandraCheck = Try {
      val result = session.execute("SELECT now() FROM system.local;")
      if (result.one().getUUID(0).version() != 1) {
        throw new IllegalStateException();
      }
    } match {
      case Success(_) =>
        HealthCheck("cassandra.session", HealthStatus.OK)
      case Failure(_) =>
        HealthCheck("cassandra.session", HealthStatus.DEFUNCT)
    }

    Vector(cassandraCheck)
  }
}
