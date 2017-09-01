package org.bitbucket.pshirshov.izumitk.akka.http.services

import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.akka.http.util.cors.CORS

@Singleton
class HttpApiCorsRootService @Inject()
(
//  childrenServices: scala.collection.immutable.Set[HttpService]
//  , requestTransformer: RequestTransformer
//  , override protected val metrics: MetricRegistry
//  , @Named("app.id") override protected val productId: AppId
//  , override implicit protected val executionContext: ExecutionContext
  override protected val httpServiceConfiguration: HttpServiceConfiguration
  , override protected val cors: CORS
) extends HttpApiRootService with CorsApiService {

}
