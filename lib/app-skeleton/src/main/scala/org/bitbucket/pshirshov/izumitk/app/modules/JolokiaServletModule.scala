package org.bitbucket.pshirshov.izumitk.app.modules

import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.jolokia.http.AgentServlet


/**
  */
final class JolokiaServletModule() extends ScalaModule {

  override def configure(): Unit = {
    val servlets = ScalaMultibinder.newSetBinder[ServletBinding](binder)
    servlets.addBinding.toInstance(ServletBinding("/jolokia/*", classOf[AgentServlet]))
  }
}
