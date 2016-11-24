package org.bitbucket.pshirshov.izumitk.akka.http.util.serialization

import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper

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
