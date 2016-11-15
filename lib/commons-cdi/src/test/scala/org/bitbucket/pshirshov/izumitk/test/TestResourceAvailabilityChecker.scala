package org.bitbucket.pshirshov.izumitk.test

import com.google.inject.Injector
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.exceptions.TestPendingException

/**
  */
trait ResourceVerifier {
  def verifyResource(injector: Injector): Unit

  def resourceUnavailable(e: Throwable): Boolean
}

@ExposedTestScope
trait TestResourceAvailabilityChecker
  extends InjectorTestBase
    with StrictLogging {
  override protected def check(injector: Injector): Unit = {
    super.check(injector)

    verifiers().foreach {
      verifier =>
        try {
          verifier.verifyResource(injector)
        } catch {
          case t: TestPendingException =>
            throw t
          case t: Throwable if verifier.resourceUnavailable(t) =>
            logger.info(s"Test skipped because of: $t")
            throw new TestPendingException
        }
    }
  }

  protected def verifiers(): Seq[ResourceVerifier] = Seq()
}
