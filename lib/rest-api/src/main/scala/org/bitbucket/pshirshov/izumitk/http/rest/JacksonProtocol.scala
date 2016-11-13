package org.bitbucket.pshirshov.izumitk.http.rest

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import com.fasterxml.jackson.databind.JsonNode
import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import akka.http.scaladsl.server.directives.JsonMarshallingDirectives

case class RestData[T](data: T, meta: Option[Map[String, JsonNode]] = None, code: Option[Int] = None)

trait SerializationProtocol {
  val protocolMapper: JacksonMapper

  implicit def entityUnmarshaler[T: Manifest]: FromEntityUnmarshaller[T]

  implicit def entityMarshaler[T <: AnyRef]: ToEntityMarshaller[T]
}

protected trait BaseJacksonProtocol extends JsonMarshallingDirectives {
  protected val protocolMapper: JacksonMapper

  protected def jacksonEntityUnmarshaller[T: Manifest]: FromEntityUnmarshaller[T] = {
    Unmarshaller
      .byteStringUnmarshaller
      .mapWithCharset(
        (data, _) =>
          protocolMapper.readValue(data.toArray)
      )
  }

  protected def jacksonEntityMarshaller[T <: AnyRef]: ToEntityMarshaller[T] = {
    Marshaller
      .StringMarshaller
      .wrap(`application/json`)(protocolMapper.writeValueAsString)
  }
}

abstract class AbstractJacksonProtocol(override val protocolMapper: JacksonMapper)
  extends SerializationProtocol with BaseJacksonProtocol {

  override implicit def entityMarshaler[T <: AnyRef]: ToEntityMarshaller[T] = {
    jacksonEntityMarshaller
  }
}

@Singleton
class JacksonProtocol @Inject()(@Named("standardMapper") override val protocolMapper: JacksonMapper)
  extends AbstractJacksonProtocol(protocolMapper) {
  override implicit def entityUnmarshaler[T: Manifest]: FromEntityUnmarshaller[T] = {
    jacksonEntityUnmarshaller[T].forContentTypes(`application/json`)
  }
}

/**
  * This class uses permissive mapper and do not require application/json content type for unmarshalling
  */
@Singleton
class PermissiveJacksonProtocol @Inject()(@Named("permissiveMapper") override val protocolMapper: JacksonMapper)
  extends AbstractJacksonProtocol(protocolMapper) {
  override implicit def entityUnmarshaler[T: Manifest]: FromEntityUnmarshaller[T] = {
    jacksonEntityUnmarshaller[T]
  }
}
