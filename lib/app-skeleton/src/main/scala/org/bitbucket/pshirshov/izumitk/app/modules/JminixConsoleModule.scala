package org.bitbucket.pshirshov.izumitk.app.modules

import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.jminix.console.servlet.MiniConsoleServlet


/**
  */
final class JminixConsoleModule() extends ScalaModule {
  override def configure(): Unit = {
    val servlets = ScalaMultibinder.newSetBinder[ServletBinding](binder)
    servlets.addBinding.toInstance(ServletBinding("/console/*", classOf[MiniConsoleServlet]))
  }
}
