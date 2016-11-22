package org.bitbucket.pshirshov.izumitk.akka.http.modules

import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.akka.http.util.logging.HttpDebugLogHandler
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope


@ExposedTestScope
class HttpDebugModule() extends ScalaModule {
  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  def loggingHttpLogHandler: HttpDebugLogHandler = new LoggingHttpDebugLogHandler()
}
