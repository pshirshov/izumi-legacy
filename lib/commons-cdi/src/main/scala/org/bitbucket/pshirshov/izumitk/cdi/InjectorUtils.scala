package org.bitbucket.pshirshov.izumitk.cdi

import com.google.inject.Injector
import com.google.inject.name.Names
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.InjectorExtensions._

import scala.collection.immutable
import scala.language.existentials
import scala.util.{Failure, Success, Try}


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

    instancesToClose.reverse.map {
      closeable =>
        if (!instancesToSkip.exists(clz => clz.isAssignableFrom(closeable.getClass))) {
          logger.info(s"Closing $closeable...")
          (closeable, Try(closeable.close()))
        } else {
          logger.info(s"Skipping $closeable...")
          (closeable, Success(Unit))
        }
    }.foreach {
      case (closeable, Failure(f)) =>
        logger.warn(s"Failed to close $closeable: $f")
      case _ =>
    }
  }
}
