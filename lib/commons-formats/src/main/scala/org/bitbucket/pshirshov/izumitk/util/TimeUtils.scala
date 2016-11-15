package org.bitbucket.pshirshov.izumitk.util

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time._
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  */
object TimeUtils {
  final val utc = ZoneId.of("UTC")
  private final val dateTimeFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  private final val dateFormat = DateTimeFormatter.ISO_DATE
  private final val timeFormat = DateTimeFormatter.ISO_TIME

  def parseFinite(s: String): FiniteDuration= FiniteDuration(Duration(s).toNanos, TimeUnit.NANOSECONDS)

  def parse(s: String): TemporalAccessor = dateTimeFormat.parse(s)
  def parseDate(s: String): TemporalAccessor = dateFormat.parse(s)

  def parse(s: Option[String]): Option[TemporalAccessor] = s.map(parse)

  def parseTs(s: String): ZonedDateTime = ZonedDateTime.parse(s, dateTimeFormat)

  def parseTs(s: Option[String]): Option[ZonedDateTime] = s.map(parseTs)

  def isoFormatUtc(timestamp: ZonedDateTime): String = dateTimeFormat.format(timestamp.withZoneSameInstant(utc))

  private def isoFormat(timestamp: ZonedDateTime) = dateTimeFormat.format(timestamp)
  def isoFormatDate(timestamp: ZonedDateTime) = dateFormat.format(timestamp)
  //def isoFormat(time: LocalTime) = timeFormat.format(time)

  def isoNow: String = isoFormat(utcNow)

  def utcNow: ZonedDateTime = ZonedDateTime.now(utc)

  def epoch: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(1), utc)

  def utcEpoch(t: Long): ZonedDateTime = {
    val instant = Instant.ofEpochSecond(t)
    ZonedDateTime.ofInstant(instant, utc)
  }

  implicit def dateTimeOrdering: Ordering[ZonedDateTime] = Ordering.fromLessThan(_ isBefore _)
}
