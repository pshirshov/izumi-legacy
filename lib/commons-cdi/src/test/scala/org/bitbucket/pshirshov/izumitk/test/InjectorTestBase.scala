package org.bitbucket.pshirshov.izumitk.test

import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.cdi.InjectorListenerModule
import org.bitbucket.pshirshov.izumitk.cdi.InjectorUtils._
import org.bitbucket.pshirshov.izumitk.config.FailingConfigLoadingStrategy
import org.scalatest.TestData
import org.scalatest.exceptions.TestPendingException
import org.slf4j.MDC

import scala.util.{Failure, Success, Try}

/**
  */
@ExposedTestScope
trait InjectorTestBase extends IzumiTestBase with StrictLogging {
  // just to avoid implicit conversions in inherited classes
  protected implicit class VisibleScalaInjector(injector: Injector) extends net.codingwell.scalaguice.InjectorExtensions.ScalaInjector(injector)

  protected val modules: Module

  protected def check(injector: Injector): Unit = {}

//  protected def checkInjectorException(exception: Throwable): Throwable = exception

  FailingConfigLoadingStrategy.init()

  protected def withInjected[Fixture : Manifest, R](test: (Fixture, Injector) => R): TestData => R = {
    withInjector {
      injector =>
        test(injector.instance[Fixture], injector)
    }
  }

  protected def withInjected[Fixture : Manifest, R](times: Int)(test: (Fixture, Injector) => R): TestData => Unit = {
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

      val injectorListenerModule = new InjectorListenerModule()

      Try(Guice.createInjector(Modules.combine(modules, testDataModule, injectorListenerModule))) match {
        case Failure(f) =>
          throw f
//          logger.warn(s"Unexpected exception during injector creation", f)
//          Try(checkInjectorException(f)) match {
//            case Failure(pending: TestPendingException) =>
//              throw pending
//            case Failure(e) =>
//              throw e
//            case Success(e) =>
//              throw e
//          }

        case Success(injector) =>
          Try(check(injector)) match {
            case Failure(f: TestPendingException) =>
              injector.shutdown()
              throw f

            case Failure(f) =>
              logger.warn(s"Unexpected exception during test pre-conditions check", f)
              injector.shutdown()
              throw f

            case Success(_) =>
              try {
                val mdcCtx = s"${getClass.getSimpleName}.${td.name}"
                MDC.put("test-name", mdcCtx)
                GlobalDiscriminator.setValue(mdcCtx)
                test(injector)
              } finally {
                injector.shutdown()
                GlobalDiscriminator.setValue(null)
                MDC.remove("test-name")
              }
          }

      }
  }
}
