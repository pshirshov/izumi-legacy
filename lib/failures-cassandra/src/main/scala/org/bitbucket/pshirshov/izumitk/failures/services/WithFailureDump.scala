package org.bitbucket.pshirshov.izumitk.failures.services

import org.bitbucket.pshirshov.izumitk.cdi.WithInjector
import net.codingwell.scalaguice.InjectorExtensions._


trait WithFailureDump {
  this: WithInjector =>

  def dumpFailures(): Unit = {
    injector.instance[FailuresDump].dump()
  }
}
