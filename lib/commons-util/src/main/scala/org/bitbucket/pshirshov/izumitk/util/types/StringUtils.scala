package org.bitbucket.pshirshov.izumitk.util.types

import scala.util.Try

object StringUtils {
  def toBoolean(s: String, defValue: Boolean): Boolean = {
    toBoolean(s).getOrElse(defValue)
  }

  def toBoolean(s: String): Option[Boolean] = {
    Try(s.toBoolean).toOption
  }

}
