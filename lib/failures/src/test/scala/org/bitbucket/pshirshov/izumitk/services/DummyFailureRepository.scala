package org.bitbucket.pshirshov.izumitk.services

import com.codahale.metrics.MetricRegistry
import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope
import org.apache.commons.lang3.exception.ExceptionUtils
import org.bitbucket.pshirshov.izumitk.cluster.model.AppId
import org.bitbucket.pshirshov.izumitk.failures.services.{FailureRecord, FailureRepository, RestoredFailureRecord}
import org.bitbucket.pshirshov.izumitk.util.types.SerializationUtils


@ExposedTestScope
@Singleton
class DummyFailureRepository @Inject()
(
  protected override val metrics: MetricRegistry
  , @Named("app.id") protected override val productId: AppId
) extends FailureRepository with StrictLogging {

  protected val failures = new scala.collection.mutable.HashMap[String, FailureRecord]()

  override def readFailure(failureId: String): Option[RestoredFailureRecord] = {
    this.synchronized {
      failures.get(failureId).map {
        failure =>
          restore(failureId, failure)
      }
    }
  }

  override protected def writeFailureRecord(id: String, failure: FailureRecord): Unit = {
    this.synchronized {
      failures.put(id, failure)
    }

    logger.error(s"Failure: $id = $failure")
    failure.causes.foreach {
      c =>
        logger.error(s"== >Failure: $id", c)
    }
  }

  override def enumerate(visitor: (RestoredFailureRecord) => Unit): Unit = {
    failures.foreach {
      case (id, value) =>
        visitor(restore(id, value))
    }
  }

  private def restore(id: String, failure: FailureRecord): RestoredFailureRecord = {
    RestoredFailureRecord(failure.data
      , Map()
      , failure.causes.map(ExceptionUtils.getStackTrace)
      , SerializationUtils.toByteArray(SerializationUtils.toByteBuffer(failure.causes))
      , id)
  }
}
