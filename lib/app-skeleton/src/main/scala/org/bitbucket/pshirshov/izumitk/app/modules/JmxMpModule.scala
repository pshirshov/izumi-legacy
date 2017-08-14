package org.bitbucket.pshirshov.izumitk.app.modules

import javax.management.MBeanServer

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.app.util.JMXUtils
import org.bitbucket.pshirshov.izumitk.app.util.JMXUtils.JMXMPContext


/**
  */
final class JmxMpModule() extends ScalaModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def jmxmp(mBeanServer: MBeanServer, @Named("@jmx.interface") host: String, @Named("@jmx.port") port: Int): JMXMPContext = {
    JMXUtils.createUsingMBeanServer(mBeanServer, host, port)
  }

}
