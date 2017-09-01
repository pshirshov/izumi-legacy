package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import akka.http.scaladsl.server.Rejection
import akka.http.scaladsl.server.directives.LogEntry
import com.google.inject.{Inject, Singleton}

import scala.collection.immutable.Seq

@Singleton
class HttpDebugLogNoopHandler @Inject()() extends HttpDebugLogHandler {
  override def accepts(rejections: Seq[Rejection]): Boolean = false

  override def handleLog(entry: LogEntry, context: HttpRequestContext): Unit = {}
}
