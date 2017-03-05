package org.bitbucket.pshirshov.izumitk.app.modules

import java.lang.management.ManagementFactory
import javax.management.MBeanServer

import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule

final class JmxModule() extends ScalaModule {
  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  def platformMbeanServer(): MBeanServer = ManagementFactory.getPlatformMBeanServer

}
