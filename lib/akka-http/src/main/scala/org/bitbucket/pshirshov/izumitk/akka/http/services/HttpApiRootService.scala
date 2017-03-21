package org.bitbucket.pshirshov.izumitk.akka.http.services

import akka.http.scaladsl.server.{Directives, Route}
import com.codahale.metrics.MetricRegistry
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.akka.http.util.logging.HttpDebug
import org.bitbucket.pshirshov.izumitk.akka.http.util.{MetricDirectives, RequestTransformer}
import org.bitbucket.pshirshov.izumitk.cluster.model.AppId

import scala.concurrent.ExecutionContext

@Singleton
class HttpApiRootService @Inject()
(
  childrenServices: scala.collection.immutable.Set[HttpService]
  , requestTransformer: RequestTransformer
  , protected val debug: HttpDebug
  , override protected val metrics: MetricRegistry
  , @Named("app.id") override protected val productId: AppId
  , override implicit protected val executionContext: ExecutionContext
) extends HttpService
  with MetricDirectives {

  import Directives._

  override val routes: Route = timerDirective {
    debug.withDebug {
      mapRequestContext(requestTransformer.requestMapper) {
        childrenServices
          .map(_.routes)
          .foldLeft[Route](reject) {
          case (acc, r) =>
            acc ~ r
        }
      }
    }
  }
}


