package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import akka.http.scaladsl.server.Rejection
import akka.http.scaladsl.server.directives.LogEntry
import com.google.inject.ImplementedBy

import scala.collection.immutable.Seq

@ImplementedBy(classOf[HttpDebugLogNoopHandler])
trait HttpDebugLogHandler {
  def accepts(rejections: Seq[Rejection]): Boolean

  def handleLog(entry: LogEntry, context: HttpRequestContext): Unit
}



