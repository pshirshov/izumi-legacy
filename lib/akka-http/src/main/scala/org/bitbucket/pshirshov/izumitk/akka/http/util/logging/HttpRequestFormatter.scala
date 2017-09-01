package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Rejection
import com.google.inject.ImplementedBy

import scala.collection.immutable.Seq

@ImplementedBy(classOf[HttpRequestFormatterImpl])
trait HttpRequestFormatter {
  def requestResponsePairToString(request: HttpRequest, response: HttpResponse): String

  def requestRejectionPairToString(request: HttpRequest, rejections: Seq[Rejection]): String
}
