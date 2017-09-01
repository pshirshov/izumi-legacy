package org.bitbucket.pshirshov.izumitk.akka.http.modules

import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.akka.http.util.logging.{HttpDebugLogHandler, LoggingHttpDebugLogHandler}
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope


@ExposedTestScope
class HttpDebugModule() extends ScalaModule {
  override def configure(): Unit = {
    bind[HttpDebugLogHandler].to[LoggingHttpDebugLogHandler]
  }
}
