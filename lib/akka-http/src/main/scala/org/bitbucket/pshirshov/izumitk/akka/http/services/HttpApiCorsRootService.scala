package org.bitbucket.pshirshov.izumitk.akka.http.services

import com.codahale.metrics.MetricRegistry
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.akka.http.util.RequestTransformer
import org.bitbucket.pshirshov.izumitk.akka.http.util.cors.CORS
import org.bitbucket.pshirshov.izumitk.akka.http.util.logging.HttpDebug
import org.bitbucket.pshirshov.izumitk.cluster.model.AppId

import scala.concurrent.ExecutionContext

@Singleton
class HttpApiCorsRootService @Inject()
(
  childrenServices: scala.collection.immutable.Set[HttpService]
  , requestTransformer: RequestTransformer
  , override protected val debug: HttpDebug
  , override protected val metrics: MetricRegistry
  , @Named("app.id") override protected val productId: AppId
  , override implicit protected val executionContext: ExecutionContext
  , override protected val cors: CORS
) extends HttpApiRootService(childrenServices, requestTransformer, debug, metrics, productId, executionContext) with CorsApiService {
}
