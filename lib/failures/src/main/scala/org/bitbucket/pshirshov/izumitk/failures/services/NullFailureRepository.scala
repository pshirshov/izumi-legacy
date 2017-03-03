package org.bitbucket.pshirshov.izumitk.failures.services

import com.codahale.metrics.MetricRegistry
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.model.cluster.AppId

/**
  */
@Singleton
class NullFailureRepository @Inject()
(
  protected override val metrics: MetricRegistry
  , @Named("app.id") protected override val productId: AppId
) extends FailureRepository {
  override def readFailure(failureId: String): Option[RestoredFailureRecord] = None

  override def enumerate(visitor: (RestoredFailureRecord) => Unit): Unit = {
  }

  override protected def writeFailureRecord(id: String, failure: FailureRecord): Unit = {

  }
}
