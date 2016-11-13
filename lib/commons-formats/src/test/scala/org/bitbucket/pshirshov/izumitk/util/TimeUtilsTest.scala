package org.bitbucket.pshirshov.izumitk.util

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
  }

}
