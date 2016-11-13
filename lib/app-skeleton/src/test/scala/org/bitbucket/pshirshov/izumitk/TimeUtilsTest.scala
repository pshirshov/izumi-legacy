package org.bitbucket.pshirshov.izumitk

import org.bitbucket.pshirshov.izumitk.test.IzumiTestBase
import org.bitbucket.pshirshov.izumitk.util.TimeUtils

import scala.concurrent.duration._
import scala.language.postfixOps

class TimeUtilsTest extends IzumiTestBase {
  "Duration Utils" must {
    "load finite duration" in {
      td =>
        assert(TimeUtils.parseFinite("1 seconds") == (1 second))
    }

    "fail to load infinite duration" in {
      td =>
        intercept[IllegalArgumentException] {
          TimeUtils.parseFinite("Inf")
        }
    }
  }

}
