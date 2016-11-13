package org.bitbucket.pshirshov.izumitk.http.util

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.{HttpEntity, HttpRequest}
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

trait Marshalling {
  protected def bodyToStringRequestUnmarshaller = {
    new FromRequestUnmarshaller[String] {
      override def apply(value: HttpRequest)(implicit ec: ExecutionContext, materializer: Materializer): Future[String] = {
        bodyToStringUnmarshaller(value.entity)(ec, materializer)
      }
    }
  }

  protected val marshallingTimeout = FiniteDuration.apply(5, TimeUnit.SECONDS)
  protected def bodyToStringUnmarshaller(entity1: HttpEntity)(implicit ec: ExecutionContext, materializer: Materializer): Future[String] = {
    entity1.toStrict(marshallingTimeout).map({
      e =>
        e.getData().utf8String
    })(ec)
  }
}
