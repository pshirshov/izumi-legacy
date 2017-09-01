package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.logRequestResult
import akka.http.scaladsl.server.directives.LogEntry
import akka.http.scaladsl.server.{Route, RouteResult}
import com.google.inject.ImplementedBy


@ImplementedBy(classOf[HttpDebugDirectivesImpl])
trait HttpDebugDirectives {
  protected def contextToString(context: HttpRequestContext): String

  protected def stringToContext(string: String): HttpRequestContext

  protected def isRequestsDebugEnabled: Boolean

  protected def requestResponseLog(request: HttpRequest, result: RouteResult, context: HttpRequestContext): Option[LogEntry]


  def withDebug(route: Route): Route = {
    if (isRequestsDebugEnabled) {
      logRequestResult(show()) {
        route
      }
    } else {
      route
    }
  }

  def withDebugContext(context: HttpRequestContext)(route: Route): Route = {
    import akka.http.scaladsl.server.directives.BasicDirectives._
    mapRequestContext(rc => rc.mapRequest(addContextPseudoHeader(context)))(route)
  }

  protected def show(): HttpRequest => RouteResult => Option[LogEntry] = {
    request =>
      result =>
        val context = request.headers
          .filter(_.is(DebugContextHeader.NAME_LOWERCASE))
          .map(s => stringToContext(s.value()))
          .headOption.getOrElse(HttpRequestContext.Empty)

        val withoutHeader = request.withHeaders(withoutDebugHeader(request))
        requestResponseLog(withoutHeader, result, context)
  }

  private def addContextPseudoHeader(context: HttpRequestContext)(req: HttpRequest): HttpRequest = {
    req.withHeaders(withoutDebugHeader(req) :+ RawHeader(DebugContextHeader.NAME, contextToString(context)))
  }

  private def withoutDebugHeader(req: HttpRequest) = {
    req.headers.filter(_.isNot(DebugContextHeader.NAME_LOWERCASE))
  }
}

object DebugContextHeader {
  final val NAME = "X-Debug-Context"
  final val NAME_LOWERCASE = NAME.toLowerCase
}
