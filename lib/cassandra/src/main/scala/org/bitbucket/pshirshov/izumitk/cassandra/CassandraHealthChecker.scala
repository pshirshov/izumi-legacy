package org.bitbucket.pshirshov.izumitk.cassandra

import com.datastax.driver.core.Session
import com.google.inject.{Inject, Injector, Singleton}
import org.bitbucket.pshirshov.izumitk.{HealthCheck, HealthChecker, HealthStatus}

import scala.util.{Failure, Success, Try}


@Singleton
class CassandraHealthChecker @Inject()
(
  injector: Injector
) extends HealthChecker {
  override def healthCheck(): Vector[HealthCheck] = {
    import net.codingwell.scalaguice.InjectorExtensions._

    val cassandraCheck = Try {
      val session = injector.instance[Session]
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
