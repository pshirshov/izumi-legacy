package org.bitbucket.pshirshov.izumitk.util

import java.time.ZoneOffset
import java.util.Date

import org.scalamock.scalatest.MockFactory
import org.scalatest.WordSpec

import scala.concurrent.duration._
import scala.language.postfixOps

// TestBase is not available from here
class TimeUtilsTest extends WordSpec with MockFactory {
  "Duration Utils" must {
    "load finite duration" in {
      assert(TimeUtils.parseFinite("1 seconds") == (1 second))
    }

    "fail to load infinite duration" in {
      intercept[IllegalArgumentException] {
        TimeUtils.parseFinite("Inf")
      }
    }

    "produce & parse ISO timestamps" in {
      val ts = TimeUtils.utcNow
      val tss = TimeUtils.isoFormatUtc(ts)
      assert(TimeUtils.parseTs(tss).isEqual(ts))
    }

    "restore timestamp from epoch millis" in {
      val now = TimeUtils.utcNow
      assert(TimeUtils.utcEpochMillies(now.toInstant.toEpochMilli).isEqual(now))
      assert(TimeUtils.utcEpochSeconds(now.toEpochSecond).isEqual(now.withNano(0)))
    }

    "convert java.util.Date to timestamp" in {
      val ts = TimeUtils.utcNow
      val shifted = ts.withZoneSameInstant(ZoneOffset.ofHours(14))
      val jts = Date.from(shifted.toInstant)
      val restored = TimeUtils.toTsAsUtc(jts)
      assert(restored.isEqual(ts))
    }
  }

}
