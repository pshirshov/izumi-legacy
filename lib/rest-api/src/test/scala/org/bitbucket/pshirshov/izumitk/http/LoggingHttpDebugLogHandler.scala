package org.bitbucket.pshirshov.izumitk.http

import akka.http.scaladsl.server.directives.LogEntry
import org.bitbucket.pshirshov.izumitk.http.rest.HttpDebugLogHandler
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope
import com.typesafe.scalalogging.StrictLogging


@ExposedTestScope
class LoggingHttpDebugLogHandler extends HttpDebugLogHandler with StrictLogging {
  override def handleLog(entry: LogEntry): Unit = {
    logger.info(entry.obj.toString)
  }
}
