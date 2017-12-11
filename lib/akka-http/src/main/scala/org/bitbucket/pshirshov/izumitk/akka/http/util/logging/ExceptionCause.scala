package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import scala.util.control.NonFatal


object ExceptionCause {
  def unapply(rejection: AnyRef): Option[Throwable] = {
    try {
      val causeMethod = rejection.getClass.getMethod("cause", Array[Class[_]](): _*)
      causeMethod.invoke(rejection) match {
        case t: Throwable =>
          Option(t)
        case _ =>
          None
      }
    } catch {
      case NonFatal(_) =>
        None
    }
  }
}
