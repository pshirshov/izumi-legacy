package org.bitbucket.pshirshov.izumitk.http.hal.serializer.links

import akka.http.scaladsl.model.HttpRequest


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
