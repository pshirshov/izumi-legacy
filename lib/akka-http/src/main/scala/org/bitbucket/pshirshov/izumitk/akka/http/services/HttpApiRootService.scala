package org.bitbucket.pshirshov.izumitk.akka.http.services

import akka.http.scaladsl.server.{Directives, Route}
import com.codahale.metrics.MetricRegistry
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.akka.http.util.logging.HttpDebugDirectives
import org.bitbucket.pshirshov.izumitk.akka.http.util.{MetricDirectives, RequestTransformer}
import org.bitbucket.pshirshov.izumitk.cluster.model.AppId

import scala.concurrent.ExecutionContext

@Singleton
class HttpServiceConfiguration @Inject()
(
  val childrenServices: scala.collection.immutable.Set[HttpService]
  , val requestTransformer: RequestTransformer
  , val metrics: MetricRegistry
  , val httpDebugDirectives: HttpDebugDirectives
  , @Named("app.id") val productId: AppId
  , implicit val executionContext: ExecutionContext
) {

}

trait HttpApiRootService
  extends HttpService
    with MetricDirectives {

  protected def httpServiceConfiguration: HttpServiceConfiguration

  import Directives._

  override def routes: Route = {
    val config = httpServiceConfiguration
    import config._

    withoutEndpointName {
      timerDirectiveWithSuffix("-io") {
        httpDebugDirectives.withDebug {
          timerDirective {
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
    }
  }
}


