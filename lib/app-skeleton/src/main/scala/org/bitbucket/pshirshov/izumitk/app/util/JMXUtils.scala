package org.bitbucket.pshirshov.izumitk.app.util

import java.lang.management.{ManagementFactory, PlatformManagedObject}

import com.sun.management.HotSpotDiagnosticMXBean
import javax.management.remote.{JMXConnectorServer, JMXConnectorServerFactory, JMXServiceURL}
import javax.management.{InstanceAlreadyExistsException, MBeanServer, ObjectName}
import com.typesafe.scalalogging.StrictLogging
import sun.management.ManagementFactoryHelper

import scala.collection.JavaConverters._

object JMXUtils extends StrictLogging {

  def createUsingMBeanServer(mbs: MBeanServer, hostname: String, port: Int): JMXMPContext = {
    configure(mbs)

    val url = new JMXServiceURL("jmxmp", hostname, port)
    val cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs)
    JMXMPContext(mbs, cs)
  }

  private def configure(mbs: MBeanServer): MBeanServer = {
    try {
      val platformBeans = new java.util.ArrayList[PlatformManagedObject]()
      platformBeans.addAll(ManagementFactory.getGarbageCollectorMXBeans)
      platformBeans.addAll(ManagementFactory.getMemoryManagerMXBeans)
      platformBeans.addAll(ManagementFactory.getMemoryPoolMXBeans)
      platformBeans.add(ManagementFactory.getClassLoadingMXBean)
      platformBeans.add(ManagementFactory.getCompilationMXBean)
      platformBeans.add(ManagementFactory.getMemoryMXBean)
      platformBeans.add(ManagementFactory.getOperatingSystemMXBean)
      platformBeans.add(ManagementFactory.getRuntimeMXBean)
      platformBeans.add(ManagementFactory.getThreadMXBean)
      platformBeans.addAll(ManagementFactoryHelper.getBufferPoolMXBeans)
      platformBeans.add(ManagementFactory.getPlatformMXBean(classOf[HotSpotDiagnosticMXBean]))

      val registered = new java.util.HashSet[ObjectName]()
      for (bean <- platformBeans.asScala) {
        val objectName = bean.getObjectName
        if (registered.add(objectName)) {
          try {
            mbs.registerMBean(bean, objectName)
          } catch {
            case ignored: InstanceAlreadyExistsException =>
          }
        } else {
          logger.warn(s"MXBean already registered: $objectName")
        }
      }
    } catch {
      case e: Exception =>
        logger.error("Failed to register JVM beans", e)
    }
    mbs
  }

  case class JMXMPContext(mBeanServer: MBeanServer, jMXConnectorServer: JMXConnectorServer)

}
