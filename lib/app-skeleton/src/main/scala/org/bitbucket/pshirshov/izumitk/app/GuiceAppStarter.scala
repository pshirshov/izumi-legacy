package org.bitbucket.pshirshov.izumitk.app

import com.google.inject.{Guice, Injector}
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.app.util.SysSignals
import org.bitbucket.pshirshov.izumitk.cdi.InjectorUtils._
import org.bitbucket.pshirshov.izumitk.cdi.{BunchOfModules, ModuleUtil, WithInjector}
import sun.misc.Signal

abstract class GuiceAppStarter
  extends SysSignals
    with StrictLogging
    with WithInjector
{
  protected final lazy val injector: Injector = {
    val modules = injectorModules()
    val appModule = ModuleUtil.multipleOverride(modules)
    Guice.createInjector(appModule)
  }

  protected def injectorModules(): Seq[BunchOfModules]

  def run(): Unit = {
    try {
      installHandlers()
      doRun()
    } catch {
      case e: Throwable =>
        logger.error(s"Startup failed", e)
        System.exit(1)
    }
  }

  override protected def onShutdown(): Unit = {
    logger.warn("Shutting down the application...")
    injector.shutdown()
  }

  override protected def onSignal(signal: Signal): Unit = {
    if (signal.getName == "INT") {
      logger.warn("Got SIGINT, exiting...")
      System.exit(1)
    }
  }

  protected def doRun(): Unit
}
