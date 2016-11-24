package org.bitbucket.pshirshov.izumitk.akka.http.util.serialization

import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper


@Singleton
class JacksonProtocol @Inject()(@Named("standardMapper") override val protocolMapper: JacksonMapper)
  extends AbstractJacksonProtocol(protocolMapper) {
  override implicit def entityUnmarshaler[T: Manifest]: FromEntityUnmarshaller[T] = {
    jacksonEntityUnmarshaller[T].forContentTypes(`application/json`)
  }
}
