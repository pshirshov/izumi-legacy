package org.bitbucket.pshirshov.izumitk.http.utils

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.implicitConversions

trait TimeoutUtils {
  implicit def toFinite(d: Duration): FiniteDuration = FiniteDuration(d.length, d.unit)
}
