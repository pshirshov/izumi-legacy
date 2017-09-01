package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import akka.http.scaladsl.server.Rejection
import akka.http.scaladsl.server.directives.LogEntry
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope

import scala.collection.immutable


@ExposedTestScope
@Singleton
class LoggingHttpDebugLogHandler @Inject()
(
  @Named("@http.debug.rejections") printAll: Boolean
)extends HttpDebugLogHandler with StrictLogging {
  override def accepts(rejections: immutable.Seq[Rejection]): Boolean = {
    rejections.nonEmpty
    // rejections.nonEmpty
    // !rejections.exists(_.isInstanceOf[MethodRejection])
    // MissingQueryParamRejection
  }

  override def handleLog(entry: LogEntry, context: HttpRequestContext): Unit = {
    logger.info(entry.obj.toString)
  }
}
