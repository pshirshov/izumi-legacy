package org.bitbucket.pshirshov.izumitk.services


import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import com.codahale.metrics.MetricRegistry
import com.google.common.io.BaseEncoding
import com.typesafe.scalalogging.StrictLogging

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
  protected val metrics: MetricRegistry
  protected val productId: String

  private val critPrefix = s"$productId-crit"
  /**
    * Never throws
    */
  final def recordFailure(failure: FailureRecord): String = {
    metrics.counter(critPrefix).inc()
    failure.causes.foreach {
      p =>
        metrics.counter(s"$critPrefix-${p.getClass.getCanonicalName}").inc()
    }

    val id = generateRandomId
    Try(writeFailureRecord(id, failure)) match {
      case Success(_) =>
        logger.error(s"Got failure: $failure", failure.causes.headOption.orNull)
        id

      case Failure(t) =>
        metrics.counter(s"$critPrefix-repository").inc()
        val failedId = transformIdAfterFailedSave(id)
        logger.error(s"$failedId: Failed while writing failure", t)
        logger.error(s"$failedId: original failure is $failure", failure.causes.headOption.orNull)
        failedId
    }
  }

  def readFailure(failureId: String): Option[RestoredFailureRecord]

  def enumerate(visitor: RestoredFailureRecord => Unit): Unit

  protected def transformIdAfterFailedSave(id: String): String = {
    s"FAILED:$id"
  }

  protected def generateRandomId: String = {
    FailureRepository.createHumanReadableId()
  }

  protected def writeFailureRecord(id: String, failure: FailureRecord): Unit
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