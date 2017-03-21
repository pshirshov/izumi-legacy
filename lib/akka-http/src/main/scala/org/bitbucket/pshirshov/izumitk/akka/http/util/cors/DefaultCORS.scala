package org.bitbucket.pshirshov.izumitk.akka.http.util.cors

import akka.http.scaladsl.model.headers.RawHeader
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}

@Singleton
class DefaultCORS @Inject()
(
  @Named("headers.cors") override val corsHeaders: Seq[RawHeader]
) extends CORS {

}
