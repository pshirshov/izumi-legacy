package org.bitbucket.pshirshov.izumitk.app.modules

import java.lang.management.ManagementFactory
import javax.management.MBeanServer

import com.codahale.metrics.{JmxReporter, MetricRegistry}
import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule


/**
  */
final class MetricsModule() extends ScalaModule {

  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  def registry: MetricRegistry = new MetricRegistry()

  @Provides
  @Singleton
  def jmxReporter(registry: MetricRegistry, mBeanServer: MBeanServer): JmxReporter = {
    JmxReporter
      .forRegistry(registry)
      .registerWith(mBeanServer)
      .build()
  }

}

final class JmxModule() extends ScalaModule {
  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  def platformMbeanServer(): MBeanServer = ManagementFactory.getPlatformMBeanServer

}
