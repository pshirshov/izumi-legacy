package org.bitbucket.pshirshov.izumitk.akka.http.util.serialization

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper

/**
  */
abstract class AbstractJacksonProtocol(override val protocolMapper: JacksonMapper)
  extends SerializationProtocol with BaseJacksonProtocol {

  override implicit def entityMarshaler[T <: AnyRef]: ToEntityMarshaller[T] = {
    jacksonEntityMarshaller
  }
}
