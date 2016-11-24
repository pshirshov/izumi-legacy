package org.bitbucket.pshirshov.izumitk.akka.http.util.serialization

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.server.directives.JsonMarshallingDirectives
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper

/**
  */
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
