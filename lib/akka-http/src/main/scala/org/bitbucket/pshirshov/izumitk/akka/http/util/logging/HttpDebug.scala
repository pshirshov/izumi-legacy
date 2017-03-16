package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import java.util.concurrent.TimeUnit

import akka.event.Logging
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry}
import akka.stream.Materializer
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.StrictLogging

import scala.collection.immutable.Seq
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}
import scala.language.{postfixOps, reflectiveCalls}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

// we need that because default logger is common for all the akka, not specific for http
trait HttpDebugLogHandler {
  def handleLog(entry: LogEntry)
}

class NoopHttpDebugLogHandler extends HttpDebugLogHandler {
  override def handleLog(entry: LogEntry): Unit = {}
}

@Singleton
class HttpDebug @Inject()
(
  @Named("@http.debug") protected val isDebugMode: Boolean
  , @Named("@http.debug-printall") protected val printAll: Boolean
  , protected val httpDebugLogHandler: HttpDebugLogHandler
  , protected implicit val materializer: Materializer
  , protected implicit val executionContext: ExecutionContext
) extends DebuggingDirectives
  with StrictLogging {

  protected val loggingLevel: Logging.LogLevel = Logging.InfoLevel
  protected val debugMarker: String = "HTTP-API"
  protected val marshallingTimeout: FiniteDuration = FiniteDuration.apply(5, TimeUnit.SECONDS)
  private val splitterSize = 120

  // fix for akka bug: https://github.com/akka/akka/issues/19981
  // repeats on small-sized requests
  private val toStrict = {
    import Directives._

    mapInnerRoute { innerRoute =>
      extractRequest { req =>
        onSuccess(req.toStrict(marshallingTimeout)) { strictReq =>
          mapRequest(_ => strictReq) {
            innerRoute
          }
        }
      }
    }
  }

  def withDebug(route: Route): Route = {
    toStrict {
      if (isDebugMode) {
        logRequestResult(show) {
          route
        }
      } else {
        route
      }
    }
  }

  def debug(marker: String): Directive0 = {
    import Directives._

    mapInnerRoute {
      route: Route =>
        ctx: RequestContext =>
          logger.debug(s"Route $marker: unmatched path is ${ctx.unmatchedPath}, request is ${formatRequest(ctx.request)}")

          Try(route(ctx)) match {
            case Success(s) =>
              logger.debug(s"Route $marker: success: $s")
              s
            case Failure(f) =>
              logger.debug(s"Route $marker: route failed", f)
              throw f
          }
    }
  }

  private def show: HttpRequest => RouteResult => Option[LogEntry]

  = {
    request =>
      result =>
        requestResponseLog(request, result)
  }

  private def requestResponseLog(request: HttpRequest, result: RouteResult)

  = {
    val out: Option[String] = result match {
      case complete: Complete =>
        Some(requestResponsePairToString(request, complete.response))

      case Rejected(rejections) =>
        // TODO: always log rejections? Looks like that non-matching requests are rejected with empty rejections list
        if (printAll || (rejections.nonEmpty && !rejections.exists(_.isInstanceOf[MethodRejection]))) {
          Some(requestRejectionPairToString(request, rejections))
        } else {
          None
        }
    }

    out match {
      case Some(repr) =>
        val entry = LogEntry(repr, debugMarker, loggingLevel)
        httpDebugLogHandler.handleLog(entry)
        Some(entry)
      case None =>
        None
    }
  }

  private def requestResponsePairToString(request: HttpRequest, response: HttpResponse): String

  = {
    formatRequest(request) +
      formatResponse(response)
  }

  private def formatResponse(response: HttpResponse): String

  = {
    val stringBuilder = new StringBuilder()
    addMessage(stringBuilder, s"RESPONSE", '-')
    stringBuilder.append('\n')
    if (response.headers.nonEmpty) {
      stringBuilder.append(response.headers.mkString("\n"))
      stringBuilder.append('\n')
    }
    stringBuilder.append('\n')
    stringBuilder.append(entityToString(response.entity, s"code: ${response.status}, "))
    stringBuilder.append('\n')
    stringBuilder.append("=" * splitterSize)
    stringBuilder.toString()
  }

  private def formatRequest(request: HttpRequest): String

  = {
    val stringBuilder = new StringBuilder()
    stringBuilder.append('\n')
    addMessage(stringBuilder, s"REQUEST", '=')
    stringBuilder.append('\n')
    stringBuilder.append(s"${request.method.name} ${request.uri}")
    stringBuilder.append('\n')

    if (request.headers.nonEmpty) {
      stringBuilder.append(request.headers.mkString("\n"))
      stringBuilder.append('\n')
    }
    stringBuilder.append('\n')

    stringBuilder.append(entityToString(request.entity, ""))
    stringBuilder.append('\n')
    stringBuilder.toString()
  }

  private def entityToString(entity: HttpEntity, moreData: String)

  = {
    val strict = Await.result(entity.toStrict(marshallingTimeout), marshallingTimeout)
    val asString = strict.getData().utf8String

    val stringBuilder = new StringBuilder()
    if (asString.nonEmpty) {
      stringBuilder.append(asString)
      stringBuilder.append('\n')
    }
    addMessage(stringBuilder, s"${moreData}content type: ${entity.contentType}, size: ${asString.length}", '-')
    stringBuilder.toString()
  }

  private def addMessage(stringBuilder: StringBuilder, message: String, filler: Char)

  = {
    val fillersSize = (splitterSize - message.length - 2) / 2
    stringBuilder.append(filler.toString * fillersSize)
    stringBuilder.append(' ')
    stringBuilder.append(message)
    stringBuilder.append(' ')
    stringBuilder.append(filler.toString * fillersSize)
  }

  private def requestRejectionPairToString(request: HttpRequest, rejections: Seq[Rejection]): String

  = {
    val stringBuilder = new StringBuilder()
    stringBuilder.append(formatRequest(request))
    addMessage(stringBuilder, s"RESPONSE (REJECTION)", '-')
    stringBuilder.append('\n')
    stringBuilder.append(formatRejection(rejections))
    stringBuilder.append('\n')
    stringBuilder.append("=" * splitterSize)
    stringBuilder.toString()
  }

  private def formatRejection(rejections: Seq[Rejection]): String

  = {
    rejections.map {
      case r@ExceptionCause(t) =>
        s"==> Rejection caused by Exception: $r, $t"

      case r =>
        s"==> Rejection: $r"
    }.mkString("\n")
  }
}

object ExceptionCause {
  def unapply(rejection: AnyRef): Option[AnyRef] = {
    try {
      val causeMethod = rejection.getClass.getMethod("cause", Array[Class[_]](): _*)
      causeMethod.invoke(rejection) match {
        case t: Throwable =>
          Option(t)
        case _ =>
          None
      }
    } catch {
      case NonFatal(_) =>
        None
    }
  }
}
