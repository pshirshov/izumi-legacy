package org.bitbucket.pshirshov.izumitk.cdi

import java.util.concurrent.{ConcurrentLinkedQueue, ExecutorService}

import com.google.inject.Provides
import com.google.inject.matcher.Matchers
import com.google.inject.name.Named
import com.google.inject.spi.ProvisionListener
import com.google.inject.spi.ProvisionListener.ProvisionInvocation
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule
import scala.collection.JavaConverters._

class InjectorListenerModule extends ScalaModule with StrictLogging {
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

        val closeables = toCloseable(obj)

        if (closeables.nonEmpty) {
          logger.debug(s"Closeable instance recorded: $closeables")
        }

        provisions.addAll(closeables.asJava)
      }
    })
  }

  private def toCloseable[T](obj: T): Seq[AutoCloseable] = {
    Seq(obj).collect {
      case ac: AutoCloseable =>
        ac
      case es: ExecutorService =>
        ExecutorClosingAdapter(es)
    }
  }

  @Provides
  @Named("closeableObjects")
  def closeableObjects(): Seq[AutoCloseable] = provisions.asScala.toSeq
}
