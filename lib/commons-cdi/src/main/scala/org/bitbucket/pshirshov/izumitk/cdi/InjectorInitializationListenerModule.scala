package org.bitbucket.pshirshov.izumitk.cdi

import java.util.concurrent.ConcurrentLinkedQueue

import com.google.inject.matcher.Matchers
import com.google.inject.spi.ProvisionListener
import com.google.inject.spi.ProvisionListener.ProvisionInvocation
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule

class InjectorInitializationListenerModule extends ScalaModule with StrictLogging {
  private val provisions = new ConcurrentLinkedQueue[AutoCloseable]()

  override def configure(): Unit = {
    binder().bindListener(Matchers.any(), new ProvisionListener {
      override def onProvision[T](provision: ProvisionInvocation[T]): Unit = {
        val obj = provision.provision()
        obj match {
          case initializable: Initializable =>
            logger.debug(s"Initializing $initializable...")
            initializable.init()

          case _ =>
        }
      }
    })
  }
}
