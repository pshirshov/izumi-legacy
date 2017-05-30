package org.bitbucket.pshirshov.izumitk.failures.services

import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import com.codahale.metrics.MetricRegistry
import com.google.common.io.BaseEncoding
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.cluster.model.AppId

import scala.util.{Failure, Success, Try}

case class FailureRecord(data: Map[String, String]
                         , causes: Vector[Throwable]
                        )

case class RestoredFailureRecord(
                                  data: Map[String, String]
                                  , meta: Map[String, String]
                                  , stacktraces: Seq[String]
                                  , causes: Array[Byte]
                                  , id: String
                                )

object FailureRecord {
  def apply(t: Throwable): FailureRecord = FailureRecord(Map(), Vector(t))

  def apply(t: Iterable[Throwable]): FailureRecord = FailureRecord(Map(), t.toVector)
}

trait FailureRepository extends StrictLogging {
  protected def metrics: MetricRegistry
  protected def productId: AppId

  private val critPrefix = s"${productId.id}-crit"

  def readFailure(failureId: String): Option[RestoredFailureRecord]

  def enumerate(visitor: RestoredFailureRecord => Unit): Unit

  protected def writeFailureRecord(id: String, failure: FailureRecord): Unit
  
  /**
    * Never throws
    */
  final def recordFailure(failure: FailureRecord): String = {
    metrics.counter(critPrefix).inc()

    failure.causes.foreach {
      p =>
        metrics.counter(s"$critPrefix-${p.getClass.getCanonicalName}").inc()
    }

    val id = generateRandomId()
    Try(writeFailureRecord(id, failure)) match {
      case Success(_) =>
        handleSuccessfulSave(failure)
        id

      case Failure(t) =>
        metrics.counter(s"$critPrefix-repository").inc()
        val failedId = transformIdAfterFailedSave(id)
        handleFailedSave(failure, t, failedId)
        failedId
    }
  }

  protected def handleSuccessfulSave(failure: FailureRecord): Unit = {
    logger.error(s"Got failure: $failure", failure.causes.headOption.orNull)
  }

  protected def handleFailedSave(failure: FailureRecord, repositoryFailure: Throwable, failedId: String): Unit = {
    logger.error(s"$failedId: Failed while writing failure", repositoryFailure)
    logger.error(s"$failedId: original failure is $failure", failure.causes.headOption.orNull)
  }

  protected def transformIdAfterFailedSave(id: String): String = {
    s"FAILED:$id"
  }

  protected def generateRandomId(): String = {
    FailureRepository.createHumanReadableId()
  }
}


object FailureRepository {
  private val formatter = DateTimeFormatter.ofPattern("MMddHHmm:")

  private def createHumanReadableId() = {
    LocalDateTime.now().format(formatter) + BaseEncoding.base32().omitPadding().encode(uuid())
  }

  private def uuid() = {
    val uuid = UUID.randomUUID()
    val hi = uuid.getMostSignificantBits
    val lo = uuid.getLeastSignificantBits
    val l1 = hi ^ lo
    val l2 = l1 >> 32
    ByteBuffer.allocate(4).putInt((l2 ^ l1).toInt).array()
  }
}
