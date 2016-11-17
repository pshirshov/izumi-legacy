package org.bitbucket.pshirshov.izumitk.http.modules

import com.google.inject.{Provides, Singleton}
import org.bitbucket.pshirshov.izumitk.http.LoggingHttpDebugLogHandler
import org.bitbucket.pshirshov.izumitk.http.rest.HttpDebugLogHandler
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope
import net.codingwell.scalaguice.ScalaModule


@ExposedTestScope
class HttpDebugModule() extends ScalaModule {
  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  def loggingHttpLogHandler: HttpDebugLogHandler = new LoggingHttpDebugLogHandler()
}
