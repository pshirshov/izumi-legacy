package org.bitbucket.pshirshov.izumitk.test

import java.util.concurrent.atomic.AtomicReference

import com.google.inject.{Guice, Injector, Module}
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.cdi.{InjectorCloseablesRecorderListenerModule, InjectorInitializationListenerModule, WithScalaInjector}
import org.bitbucket.pshirshov.izumitk.config.FailingConfigLoadingStrategy
import org.scalatest.exceptions.TestPendingException
import org.scalatest.{BeforeAndAfterAll, TestData}
import org.slf4j.MDC

import scala.util.{Failure, Success, Try}


@ExposedTestScope
trait InjectorTestBase
  extends IzumiTestBase
    with WithScalaInjector
    with BeforeAndAfterAll
    with StrictLogging {

  FailingConfigLoadingStrategy.init()

  protected def mainTestModule: Module

  protected final val cachedInjector: AtomicReference[Try[Injector]] = new AtomicReference[Try[Injector]]()


  override protected def beforeAll(): Unit = {
    super.beforeAll()
    cachedInjector.set(Try(Guice.createInjector(
      mainTestModule
      , new InjectorInitializationListenerModule()
      , new InjectorCloseablesRecorderListenerModule()
    )))
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    import org.bitbucket.pshirshov.izumitk.cdi.InjectorUtils._
    cachedInjector.get().map(_.shutdown())
  }

  protected def check(injector: Injector): Unit = {}

  protected def withInjected[Fixture: Manifest, R](test: (Fixture, Injector) => R): TestData => R = {
    withInjector {
      injector =>
        test(injector.instance[Fixture], injector)
    }
  }

  protected def withInjected[Fixture: Manifest, R](times: Int)(test: (Fixture, Injector) => R): TestData => Unit = {
    withInjector {
      injector =>
        for (n <- 1 to times) {
          test(injector.instance[Fixture], injector)
        }
    }
  }

  protected final def withInjector[T](test: (Injector) => T): TestData => T = {
    td =>
      val testDataModule = new ScalaModule {
        override def configure(): Unit = {
          bind[FixtureParam].toInstance(td)
        }
      }

      createTestInjector(Seq(testDataModule)) match {
        case Success(injector) =>
          Try(check(injector)) match {
            case Success(_) =>
              runTest(test, td, injector)

            case Failure(f: TestPendingException) =>
              throw f

            case Failure(f) =>
              logger.warn(s"Unexpected exception during test pre-conditions check", f)
              throw f
          }

        case Failure(f) =>
          throw f
      }
  }

  protected def runTest[T](test: (Injector) => T, td: FixtureParam, injector: Injector): T = {
    try {
      val mdcCtx = s"${getClass.getSimpleName}.${td.name}"
      MDC.put("test-name", mdcCtx)
      GlobalDiscriminator.setValue(mdcCtx)
      test(injector)
    } finally {
      GlobalDiscriminator.setValue(null)
      MDC.remove("test-name")
    }
  }

  protected def createTestInjector[T](testSpecificModules: Seq[Module]): Try[Injector] = {
    import scala.collection.JavaConverters._
    cachedInjector.get().map(_.createChildInjector(testSpecificModules.asJava))
  }
}
