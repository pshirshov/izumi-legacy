package org.bitbucket.pshirshov.izumitk.akka.http.util.directives

import java.time.ZonedDateTime

import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.util.FastFuture
import org.bitbucket.pshirshov.izumitk.util.types.TimeUtils

trait WithIzumiUnmarshallers {
  protected def simpleUnmarshaller[T : Manifest](constructor: String => T): Unmarshaller[String, T] = {
    Unmarshaller.apply(_ => {
      param =>
        FastFuture.successful(constructor(param))
    })
  }

  protected implicit def timestampUnmarshaller: Unmarshaller[String, ZonedDateTime] = simpleUnmarshaller(TimeUtils.parseTs)
}
