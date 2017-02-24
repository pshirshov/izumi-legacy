package org.bitbucket.pshirshov.izumitk.cassandra.modules

import java.net.UnknownHostException

import com.datastax.driver.core.Session
import com.datastax.driver.core.exceptions.{ConnectionException, NoHostAvailableException}
import com.google.inject.Injector
import org.bitbucket.pshirshov.izumitk.test.{ExposedTestScope, ResourceVerifier, TestResourceAvailabilityChecker}
import org.bitbucket.pshirshov.izumitk.util.types.ExceptionUtils


@ExposedTestScope
trait CassandraTestBase extends TestResourceAvailabilityChecker {
  abstract override protected def verifiers(): Seq[ResourceVerifier] = super.verifiers() :+ new ResourceVerifier {
    override def verifyResource(injector: Injector): Unit = {
      injector.instance[Session].getState.getConnectedHosts
    }

    override def resourceUnavailable(e: Throwable): Boolean = {

      val unavailabilityMarkers = Seq(
        classOf[ConnectionException]
        , classOf[NoHostAvailableException]
        , classOf[UnknownHostException]
      )

      oneOf(e, unavailabilityMarkers)
    }
  }
}
