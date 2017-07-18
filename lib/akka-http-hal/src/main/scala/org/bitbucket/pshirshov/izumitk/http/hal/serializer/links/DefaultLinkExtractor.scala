package org.bitbucket.pshirshov.izumitk.http.hal.serializer.links

import akka.http.scaladsl.model.HttpRequest
import com.google.inject.{Inject, Singleton}

@Singleton
class DefaultLinkExtractor @Inject()() extends LinkExtractor {
  def extract(maybeRequest: Option[HttpRequest]): String = maybeRequest match {
    case Some(req) => if (containsForwarded(req)) {
      new ForwardedBuilder(req).build()
    } else {
      new UrlBuilder(req).build()
    }
    case None =>
      ""
  }

  private def containsForwarded(req: HttpRequest) = {
    req.headers.exists(xf => xf.lowercaseName().contains("X-Forwarded".toLowerCase))
  }
}
