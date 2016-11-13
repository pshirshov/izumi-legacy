package org.bitbucket.pshirshov.izumitk.app.modules

import javax.management.MBeanServer

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.bitbucket.pshirshov.izumitk.app.util.JMXUtils
import org.bitbucket.pshirshov.izumitk.app.util.JMXUtils.JMXMPContext
import org.jminix.console.servlet.MiniConsoleServlet
import org.jolokia.http.AgentServlet


/**
  */
final class JmxMpModule() extends ScalaModule {

  override def configure(): Unit = {
    val servlets = ScalaMultibinder.newSetBinder[ServletBinding](binder)
    servlets.addBinding.toInstance(ServletBinding("/console/*", classOf[MiniConsoleServlet]))
    servlets.addBinding.toInstance(ServletBinding("/jolokia/*", classOf[AgentServlet]))
  }

  @Provides
  @Singleton
  def jmxmp(mBeanServer: MBeanServer, @Named("@jmx.interface") host: String, @Named("@jmx.port") port: Int): JMXMPContext = {
    JMXUtils.createUsingMBeanServer(mBeanServer, host, port)
  }

}
