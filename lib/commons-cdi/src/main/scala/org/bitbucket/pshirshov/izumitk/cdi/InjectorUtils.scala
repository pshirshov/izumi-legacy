package org.bitbucket.pshirshov.izumitk.cdi

import java.util.concurrent.{ConcurrentLinkedQueue, ExecutorService, TimeUnit}

import com.google.inject.matcher.Matchers
import com.google.inject.name.{Named, Names}
import com.google.inject.spi.ProvisionListener
import com.google.inject.spi.ProvisionListener.ProvisionInvocation
import com.google.inject.{Injector, Provides}
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.InjectorExtensions._
import net.codingwell.scalaguice.ScalaModule

import scala.collection.JavaConverters._
import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.language.existentials
import scala.util.{Failure, Success, Try}

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


sealed case class ExecutorClosingAdapter(executor: ExecutorService) extends AutoCloseable {
  override def close(): Unit = {
    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)
  }
}

object InjectorUtils extends StrictLogging {
  implicit class ShutdownSupport(injector: Injector) {
    def shutdown(): Unit = InjectorUtils.shutdown(injector)
  }

  private def shutdown(injector: Injector): Unit = {
    val instancesToSkip = Try(injector.instance[immutable.Set[Class[_]]](Names.named("notCloseOnShutdown"))) match {
      case Success(value) =>
        value
      case Failure(_) =>
        immutable.Set[Class[_]]()
    }

    val instancesToClose = injector.instance[Seq[AutoCloseable]](Names.named("closeableObjects"))
    logger.info(s"Closing ${instancesToClose.size} closeables..")

    instancesToClose.map {
      closeable =>
        if (!instancesToSkip.exists(clz => clz.isAssignableFrom(closeable.getClass))) {
          logger.info(s"Closing $closeable...")
          (closeable, Try(closeable.close()))
        } else {
          logger.info(s"Skipping $closeable...")
          Success((closeable, Unit))
        }
    }.foreach {
      case (closeable, Failure(f)) =>
        logger.warn(s"Failed to close $closeable: $f")
      case _ =>
    }
  }

/*  def getSafeInstances[T: Manifest](injector: Injector): Seq[T] = {
    val triedInstances: Seq[Try[T]] = getInstances[T](injector)

    val (good, bad) = triedInstances.partition(_.isSuccess)

    bad.foreach {
      case Failure(f) =>
        logger.warn(s"Provisioning error: $f")
    }

    good.collect {
      case Success(s) =>
        s
    }
  }

  def getInstances[T:Manifest](injector: Injector): Seq[Try[T]] = {
    //logger.debug(s"INJECTOR: $this: GET STARTED")

    val clazz = manifest[T].runtimeClass.asInstanceOf[Class[T]]
    val services = new util.LinkedHashSet[Try[T]]()
    val visitor = new BindingVisitor[T](services, clazz)

    val bindings = injector.getBindings.values

    bindings.foreach {
      binding =>
        Try(binding.acceptTargetVisitor(visitor))
    }

    // reverse creation order is correct shutdown order
    //logger.debug(s"INJECTOR: $this: GET ENDED")

    services.toSeq.reverse
  }

  private class BindingVisitor[T](services: util.HashSet[Try[T]], clazz: Class[T]) extends DefaultBindingTargetVisitor[Any, Unit] {
    protected override def visitOther(binding: Binding[_]): Unit = {
      //val instanceClazz = binding.getKey.getTypeLiteral.getRawType
      //logger.debug(s"INJECTOR: $this: VISITING $binding")

      Try(binding.getProvider.get()) match {
        case s@Success(instance) =>
          if (clazz.isAssignableFrom(instance.getClass)) {
            services.add(Success(clazz.cast(instance)))
          }
        case Failure(f) =>
          services.add(Failure(f))
      }
      //logger.debug(s"INJECTOR: $this: VISITED $binding")
    }
  }*/
}
