package org.bitbucket.pshirshov.izumitk.akka.http.services

import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import com.codahale.metrics.MetricRegistry
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.akka.http.util.logging.{HttpDebugDirectives, WithToStrict}
import org.bitbucket.pshirshov.izumitk.akka.http.util.{MetricDirectives, RequestTransformer}
import org.bitbucket.pshirshov.izumitk.cluster.model.AppId

import scala.concurrent.ExecutionContext

@Singleton
class HttpServiceConfiguration @Inject()
(
  val requestTransformer: RequestTransformer
  , val metrics: MetricRegistry
  , val httpDebugDirectives: HttpDebugDirectives
  , @Named("app.id") val productId: AppId
  , implicit val executionContext: ExecutionContext
  , implicit val materializer: Materializer
) {

}

trait HttpApiRootService
  extends HttpService
    with WithToStrict
    with MetricDirectives {

  protected def httpServiceConfiguration: HttpServiceConfiguration

  protected def childrenServices: scala.collection.immutable.Set[HttpService]

  import Directives._

  override def routes: Route = {
    val config = httpServiceConfiguration
    import config._

    identity(toStrict) {
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
}


