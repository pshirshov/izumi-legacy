package org.bitbucket.pshirshov.izumitk.http.hal.serializer.links

import akka.http.scaladsl.model.{HttpHeader, HttpRequest}

// https://github.com/marcuslange/akka-http-hal/blob/master/src/main/scala/akka/http/rest/hal/Href.scala

class ForwardedBuilder(req: HttpRequest) extends BaseUriBuilder {
  def build(): String = {
    val b = new StringBuilder()

    val proto = extract("X-Forwarded-Proto").headOption.getOrElse("http")
    b.append(proto)
    b.append("://")

    val port = extract("X-Forwarded-Port")

    extractHost() match {
      case None =>
      case Some(v) =>
        val parts = v.split(':')

        if (parts.length == 2) {
          b.append(parts.head)
          appendPort(b, proto, port.headOption.getOrElse(parts.last))
        } else {
          b.append(v)
          appendPort(b, proto, port.headOption.getOrElse("80"))
        }
    }

    extract("X-Forwarded-Prefix").headOption match {
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

  protected def extract(name: String): Seq[String] = {
    req.headers.collect {
      case h: HttpHeader if h.lowercaseName() == name.toLowerCase =>
        h.value.toLowerCase.split(',')
    }.flatten
  }

  protected def extractHost(): Option[String] = {
    val hostHeader = if (req.uri.authority.host.address.length > 0) {
      Some(req.uri.authority.host.address)
    } else {
      None
    }

    extract("X-Forwarded-Host").headOption.orElse(hostHeader)
  }
}
