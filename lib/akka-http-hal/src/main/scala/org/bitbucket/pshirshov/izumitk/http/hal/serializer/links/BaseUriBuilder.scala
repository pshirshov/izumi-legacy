package org.bitbucket.pshirshov.izumitk.http.hal.serializer.links

// based on https://github.com/marcuslange/akka-http-hal/blob/master/src/main/scala/akka/http/rest/hal/Href.scala
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
