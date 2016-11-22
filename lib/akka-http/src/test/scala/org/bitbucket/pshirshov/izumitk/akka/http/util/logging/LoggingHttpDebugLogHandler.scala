package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import akka.http.scaladsl.server.directives.LogEntry
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope


@ExposedTestScope
class LoggingHttpDebugLogHandler extends HttpDebugLogHandler with StrictLogging {
  override def handleLog(entry: LogEntry): Unit = {
    logger.info(entry.obj.toString)
  }
}
