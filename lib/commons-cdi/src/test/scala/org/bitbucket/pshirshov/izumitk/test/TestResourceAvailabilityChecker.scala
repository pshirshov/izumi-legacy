package org.bitbucket.pshirshov.izumitk.test

import java.net.{InetSocketAddress, Socket, URL}

import com.google.inject.Injector
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.exceptions.TestPendingException
import resource.managed

import scala.util.{Failure, Success, Try}

/**
  */
@ExposedTestScope
trait ResourceVerifier {
  def verifyResource(injector: Injector): Unit

  def resourceUnavailable(e: Throwable): Boolean
}

@ExposedTestScope
trait ServiceAvailabilityChecker {
  this: TestResourceAvailabilityChecker =>

  def verifyServiceAvailability(uri: String, timeout: Int): Unit = {
    verifyServiceAvailability(new URL(uri), timeout)
  }

  def verifyServiceAvailability(uri: URL, timeout: Int): Unit = {
    verifyServiceAvailability(uri.getHost, uri.getPort, timeout)
  }

  def verifyServiceAvailability(host: String, port: Int, timeout: Int): Unit = {
    Try {
      managed(new Socket()).acquireAndGet {
        s =>
          s.connect(new InetSocketAddress(host, port), timeout)
      }
    } match {
      case Failure(f) =>
        logger.info(s"Test skipped because of unavailable service: $host:$port, $f")
        throw new TestPendingException
      case Success(_) =>
    }
  }
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
