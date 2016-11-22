package org.bitbucket.pshirshov.izumitk.util

import scala.collection.mutable.ArrayBuffer
//remove if not needed

object ExceptionUtils {
  def currentStack(): String = {
    format(new RuntimeException())
  }

  def format(t: Throwable): String = format(t, Set("org.bitbucket.pshirshov.izumitk"))

  def format(t: Throwable, packages: Set[String]): String = {
    val messages = causes(t).map {
      currentThrowable =>
        val origin = getStackTop(t, packages) match {
          case Some(frame) =>
            s"${frame.getFileName}:${frame.getLineNumber}"
          case _ =>
            "?"
        }
        s"${t.getMessage}@${t.getClass.getSimpleName} $origin"

    }

    messages.mkString(", due ")
  }

  def allMessages(t: Throwable): Seq[String] = {
    causes(t).map(_.getMessage)
  }

  def allClasses(t: Throwable): Seq[String] = {
    causes(t).map(_.getClass.getCanonicalName)
  }

  def causes(t: Throwable): Seq[Throwable] = {
    val ret = new ArrayBuffer[Throwable]()
    var currentThrowable = t
    while (currentThrowable != null) {
      ret.append(currentThrowable)
      currentThrowable = currentThrowable.getCause
    }
    ret
  }

  private def getStackTop(t: Throwable, acceptedPackages: Set[String]): Option[StackTraceElement] = {
    t.getStackTrace.find {
      frame =>
        !frame.isNativeMethod && acceptedPackages.exists(frame.getClassName.startsWith)
    }
  }
}
