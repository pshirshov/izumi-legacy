package org.bitbucket.pshirshov.izumitk.http.utils

import scala.concurrent.duration.{Duration, FiniteDuration}

trait TimeoutUtils {
  implicit def toFinite(d: Duration) = FiniteDuration(d.length, d.unit)
}
