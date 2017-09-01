package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import akka.event.Logging
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.LogEntry
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper


@Singleton
class HttpDebugDirectivesImpl @Inject()
(
  @Named("@http.debug.requests") override val isRequestsDebugEnabled: Boolean
  , @Named("@http.debug.rejections") debugRejections: Boolean
  , httpRequestFormatter: HttpRequestFormatter
  , httpDebugLogHandler: HttpDebugLogHandler
  , @Named("typingMapper") mapper: JacksonMapper
) extends HttpDebugDirectives {
  protected val loggingLevel: Logging.LogLevel = Logging.InfoLevel
  protected val debugMarker: String = "HTTP-API"


  override protected def contextToString(context: HttpRequestContext): String = {
    mapper.writeValueAsString(context)
  }

  override protected def stringToContext(string: String): HttpRequestContext = {
    mapper.readValue[HttpRequestContext](string)
  }


  override protected def requestResponseLog(request: HttpRequest, result: RouteResult, context: HttpRequestContext): Option[LogEntry] = {
    val out: Option[String] = result match {
      case complete: Complete =>
        Some(httpRequestFormatter.requestResponsePairToString(request, complete.response))

      case Rejected(rejections) =>
        None
        // TODO: always log rejections? Looks like that non-matching requests are rejected with empty rejections list
        if (debugRejections && httpDebugLogHandler.accepts(rejections)) {
          Some(httpRequestFormatter.requestRejectionPairToString(request, rejections))
        } else {
          None
        }
    }

    out match {
      case Some(repr) =>
        val shifted = repr.split("\n").mkString("\n    ")
        val entry = LogEntry(shifted, debugMarker, loggingLevel)
        httpDebugLogHandler.handleLog(entry, context)
        Some(entry)
      case None =>
        None
    }
  }


}
