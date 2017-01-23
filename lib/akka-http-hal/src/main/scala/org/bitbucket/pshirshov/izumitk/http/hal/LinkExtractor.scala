package org.bitbucket.pshirshov.izumitk.http.hal

import akka.http.scaladsl.model.HttpRequest

trait LinkExtractor {
  def extract(maybeRequest:Option[HttpRequest]):String
}
