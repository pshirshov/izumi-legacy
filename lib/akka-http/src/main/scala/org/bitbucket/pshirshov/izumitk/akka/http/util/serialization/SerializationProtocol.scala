package org.bitbucket.pshirshov.izumitk.akka.http.util.serialization

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper

/**
  */
trait SerializationProtocol {
  val protocolMapper: JacksonMapper

  implicit def entityUnmarshaler[T: Manifest]: FromEntityUnmarshaller[T]

  implicit def entityMarshaler[T <: AnyRef]: ToEntityMarshaller[T]
}
