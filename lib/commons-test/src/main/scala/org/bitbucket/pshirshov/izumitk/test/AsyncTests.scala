package org.bitbucket.pshirshov.izumitk.test

import org.bitbucket.pshirshov.izumitk.test.AsyncTests.AwaitDuration

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal


/**
  * Based on TestKit
  */
trait AsyncTests {
  import scala.concurrent.duration._

  private def now: FiniteDuration = System.nanoTime.nanos

  protected def awaitAssert(a: ⇒ Any, interval: Duration = 100.millis)(implicit max: AwaitDuration): Unit = {
    val _max = max.max
    val stop = now + _max

    @tailrec
    def poll(t: Duration): Unit = {
      val failed =
        try { a; false } catch {
          case NonFatal(e) ⇒
            if ((now + t) >= stop) throw e
            true
        }
      if (failed) {
        Thread.sleep(t.toMillis)
        poll((stop - now) min interval)
      }
    }

    poll(_max min interval)
  }

  protected def awaitCond(p: ⇒ Boolean, interval: Duration = 100.millis, message: String = "")(implicit max: AwaitDuration): Unit = {
    val _max = max.max
    val stop = now + _max

    @tailrec
    def poll(t: Duration): Unit = {
      if (!p) {
        assert(now < stop, s"timeout ${_max} expired: $message")
        Thread.sleep(t.toMillis)
        poll((stop - now) min interval)
      }
    }

    poll(_max min interval)
  }

}

object AsyncTests {
  case class AwaitDuration(max: Duration)
}