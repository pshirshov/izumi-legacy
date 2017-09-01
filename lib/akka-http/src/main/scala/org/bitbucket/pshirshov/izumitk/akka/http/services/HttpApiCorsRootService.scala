package org.bitbucket.pshirshov.izumitk.akka.http.services

import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.akka.http.util.cors.CORS

@Singleton
class HttpApiCorsRootService @Inject()
(
  override protected val httpServiceConfiguration: HttpServiceConfiguration
  , override protected val childrenServices: scala.collection.immutable.Set[HttpService]
  , override protected val cors: CORS
) extends HttpApiRootService with CorsApiService {

}
