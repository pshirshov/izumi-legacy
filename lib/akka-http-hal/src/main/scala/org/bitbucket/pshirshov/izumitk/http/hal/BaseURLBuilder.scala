package org.bitbucket.pshirshov.izumitk.http.hal

import com.google.inject.{Inject, Singleton}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}

// https://github.com/marcuslange/akka-http-hal/blob/master/src/main/scala/akka/http/rest/hal/Href.scala

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

trait BaseUriBuilder {
  protected def defaultPort(scheme: String): Int = {
    scheme match {
      case "http" =>
        80
      case "https" =>
        443
      case _ =>
        throw new IllegalArgumentException(s"Bad scheme: $scheme")
    }
  }

}

class ForwardedBuilder(req: HttpRequest) extends BaseUriBuilder {
  def build(): String = {
    val b = new StringBuilder()

    val proto = extract("X-Forwarded-Proto").getOrElse("http")
    b.append(proto)
    b.append("://")

    val port = extract("X-Forwarded-Port")

    extractHost() match {
      case None =>
      case Some(v) =>
        val parts = v.split(':')

        if (parts.length == 2) {
          b.append(parts.head)
          appendPort(b, proto, port.getOrElse(parts.last))
        } else {
          b.append(v)
          appendPort(b, proto, port.getOrElse("80"))
        }
    }

    extract("X-Forwarded-Prefix") match {
      case None =>
      case Some(prefix) =>
        b.append('/')
        b.append(prefix)
    }

    b.toString()
  }

  private def appendPort(b: StringBuilder, proto: String, port: String) = {
    if (port != defaultPort(proto).toString) {
      b.append(':')
      b.append(port)
    }
  }

  protected def extract(name: String): Option[String] = {
    req.headers.collectFirst {
      case h: HttpHeader if h.lowercaseName() == name.toLowerCase =>
        h.value.toLowerCase
    }
  }

  protected def extractHost(): Option[String] = {
    val hostHeader = if (req.uri.authority.host.address.length > 0) {
      Some(req.uri.authority.host.address)
    } else {
      None
    }
    
    extract("X-Forwarded-Host").orElse(hostHeader)
  }
}

class UrlBuilder(req: HttpRequest) extends BaseUriBuilder {
  def build(): String = {
    val proto = req.uri.scheme
    val host = req.uri.authority.host.address
    val port = req.uri.authority.port

    if (defaultPort(proto) == port) {
      s"$proto://$host"
    } else {
      s"$proto://$host:$port"
    }
  }
}
