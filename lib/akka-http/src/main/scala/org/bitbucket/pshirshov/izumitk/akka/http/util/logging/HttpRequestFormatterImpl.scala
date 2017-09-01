package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Rejection
import akka.stream.Materializer
import com.google.inject.{Inject, Singleton}

import scala.collection.immutable.Seq
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

@Singleton
class HttpRequestFormatterImpl @Inject()
(
  protected implicit val materializer: Materializer
) extends HttpRequestFormatter {
  protected val marshallingTimeout: FiniteDuration = FiniteDuration.apply(5, TimeUnit.SECONDS)

  private val splitterSize = 120

  def requestResponsePairToString(request: HttpRequest, response: HttpResponse): String = {
    formatRequest(request) +
      formatResponse(response)
  }

  def requestRejectionPairToString(request: HttpRequest, rejections: Seq[Rejection]): String = {
    val stringBuilder = new StringBuilder()
    stringBuilder.append(formatRequest(request))
    addMessage(stringBuilder, s"RESPONSE (REJECTION)", '-')
    stringBuilder.append('\n')
    stringBuilder.append(formatRejection(rejections))
    stringBuilder.append('\n')
    stringBuilder.append("=" * splitterSize)
    stringBuilder.toString()
  }

  private def formatResponse(response: HttpResponse): String = {
    val stringBuilder = new StringBuilder()
    addMessage(stringBuilder, s"RESPONSE", '-')
    stringBuilder.append('\n')
    if (response.headers.nonEmpty) {
      stringBuilder.append(response.headers.filter(_.renderInResponses()).mkString("\n"))
      stringBuilder.append('\n')
    }
    stringBuilder.append('\n')
    stringBuilder.append(entityToString(response.entity, s"code: ${response.status}, "))
    stringBuilder.append('\n')
    stringBuilder.append("=" * splitterSize)
    stringBuilder.toString()
  }

  private def formatRequest(request: HttpRequest): String = {
    val stringBuilder = new StringBuilder()
    stringBuilder.append('\n')
    addMessage(stringBuilder, s"REQUEST", '=')
    stringBuilder.append('\n')
    stringBuilder.append(s"${request.method.name} ${request.uri}")
    stringBuilder.append('\n')

    if (request.headers.nonEmpty) {
      stringBuilder.append(request.headers.filter(_.renderInRequests()).mkString("\n"))
      stringBuilder.append('\n')
    }
    stringBuilder.append('\n')

    stringBuilder.append(entityToString(request.entity, ""))
    stringBuilder.append('\n')
    stringBuilder.toString()
  }

  private def entityToString(entity: HttpEntity, moreData: String) = {
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

  private def addMessage(stringBuilder: StringBuilder, message: String, filler: Char) = {
    val fillersSize = (splitterSize - message.length - 2) / 2
    stringBuilder.append(filler.toString * fillersSize)
    stringBuilder.append(' ')
    stringBuilder.append(message)
    stringBuilder.append(' ')
    stringBuilder.append(filler.toString * fillersSize)
  }


  private def formatRejection(rejections: Seq[Rejection]): String = {
    rejections.map {
      case r@ExceptionCause(t) =>
        s"==> Rejection caused by Exception: $r, $t"

      case r =>
        s"==> Rejection: $r"
    }.mkString("\n")
  }
}
