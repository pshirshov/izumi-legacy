package org.bitbucket.pshirshov.izumitk.test

import com.google.inject.Injector
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.exceptions.TestPendingException

/**
  */
@ExposedTestScope
trait TestResourceAvailabilityChecker
  extends InjectorTestBase
  with StrictLogging
{
  override protected def check(injector: Injector): Unit = {
    super.check(injector)

    try {
      verifyResource(injector)
    } catch {
      case t: TestPendingException =>
        throw t
      case t: Throwable if resourceUnavailable(t)=>
        logger.info(s"Test skipped because of: $t")
        throw new TestPendingException
    }
  }

  protected def verifyResource(injector: Injector): Unit

  protected def resourceUnavailable(e: Throwable): Boolean
}
