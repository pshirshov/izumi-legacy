package org.bitbucket.pshirshov.izumitk.http.hal.serializer.links

import akka.http.scaladsl.model.HttpRequest
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[DefaultLinkExtractor])
trait LinkExtractor {
  def extract(maybeRequest:Option[HttpRequest]):String
}
