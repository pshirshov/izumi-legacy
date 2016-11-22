package org.bitbucket.pshirshov.izumitk.akka.http.util

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.directives.BasicDirectives
import akka.http.scaladsl.server.{Directive0, RequestContext, Route, RouteResult}
import com.codahale.metrics._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

trait MetricDirectives extends BasicDirectives with StrictLogging {
  def timerDirective: Directive0 = {
    mapInnerRoute {
      route: Route => ctx: RequestContext =>
        val before = System.nanoTime()

        val response: Future[RouteResult] = route(ctx)

        response.onSuccess {
          case RouteResult.Rejected(rejection) =>

          case RouteResult.Complete(r) =>
            val operationDuration = System.nanoTime() - before
            r.headers.find(_.is(MetricDirectives.ENDPOINT_NAME_HEADER)).foreach {
              h =>
                val metricName = s"$productId-${h.value()}"
                val timer = metrics.timer(metricName)
                timer.update(operationDuration, TimeUnit.NANOSECONDS)
                logger.trace(s"Metric recorded: $metricName=$operationDuration")
            }
        }

        response.map {
          case rr@RouteResult.Rejected(_) =>
            rr

          case rr@RouteResult.Complete(_) =>
            rr.copy(response = rr.response.withHeaders(rr.response.headers.filterNot(_.is(MetricDirectives.ENDPOINT_NAME_HEADER))))
        }
    }
  }

  def metered(endpointName: String): Directive0 = {
    mapInnerRoute {
      route: Route => ctx: RequestContext =>
        route(ctx).map {
          case rr@RouteResult.Rejected(_) =>
            rr

          case rr@RouteResult.Complete(_) =>
            rr.copy(response = rr.response.withHeaders(rr.response.headers :+ RawHeader(MetricDirectives.ENDPOINT_NAME_HEADER, endpointName)))
        }
    }
  }

  protected implicit val executionContext: ExecutionContext
  protected val metrics: MetricRegistry
  protected val productId: String
}

object MetricDirectives {
  final val ENDPOINT_NAME_HEADER = "x-endpoint-name"
}