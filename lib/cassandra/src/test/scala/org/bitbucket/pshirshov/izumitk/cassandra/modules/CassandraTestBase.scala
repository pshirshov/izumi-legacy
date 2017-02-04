package org.bitbucket.pshirshov.izumitk.cassandra.modules

import java.net.UnknownHostException

import com.datastax.driver.core.Session
import com.datastax.driver.core.exceptions.{ConnectionException, NoHostAvailableException}
import com.google.inject.Injector
import org.bitbucket.pshirshov.izumitk.test.{ExposedTestScope, ResourceVerifier, TestResourceAvailabilityChecker}
import org.bitbucket.pshirshov.izumitk.util.ExceptionUtils


@ExposedTestScope
trait CassandraTestBase extends TestResourceAvailabilityChecker {
  abstract override protected def verifiers(): Seq[ResourceVerifier] = super.verifiers() :+ new ResourceVerifier {
    override def verifyResource(injector: Injector): Unit = {
      injector.instance[Session].getState.getConnectedHosts
    }

    override def resourceUnavailable(e: Throwable): Boolean = {
      val causes = ExceptionUtils.causes(e).map(_.getClass)

      causes.exists(classOf[ConnectionException].isAssignableFrom) ||
      causes.exists(classOf[NoHostAvailableException].isAssignableFrom) ||
      causes.exists(classOf[UnknownHostException].isAssignableFrom)
    }
  }
}
