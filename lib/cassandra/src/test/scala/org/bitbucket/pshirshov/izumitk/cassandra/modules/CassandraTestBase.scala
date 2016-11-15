package org.bitbucket.pshirshov.izumitk.cassandra.modules

import com.datastax.driver.core.Session
import com.datastax.driver.core.exceptions.{ConnectionException, NoHostAvailableException}
import com.google.inject.Injector
import org.bitbucket.pshirshov.izumitk.test.{ExposedTestScope, ResourceVerifier, TestResourceAvailabilityChecker}


@ExposedTestScope
trait CassandraTestBase extends TestResourceAvailabilityChecker {
  abstract override protected def verifiers(): Seq[ResourceVerifier] = super.verifiers() :+ new ResourceVerifier {
    override def verifyResource(injector: Injector): Unit = {
      injector.instance[Session].getState.getConnectedHosts
    }

    override def resourceUnavailable(e: Throwable): Boolean = {
      val exceptionClass = e.getCause.getClass

      classOf[ConnectionException].isAssignableFrom(exceptionClass) ||
        classOf[NoHostAvailableException].isAssignableFrom(exceptionClass)
    }
  }
}
