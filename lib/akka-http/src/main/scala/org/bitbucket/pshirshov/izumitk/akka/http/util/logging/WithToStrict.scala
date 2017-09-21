package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.server.Directive0
import akka.stream.Materializer

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait WithToStrict {
  protected val marshallingTimeout: FiniteDuration = FiniteDuration.apply(5, TimeUnit.SECONDS)

  // fix for akka bug: https://github.com/akka/akka/issues/19981
  // repeats on small-sized requests
  protected def toStrict()(implicit ec: ExecutionContext, m: Materializer): Directive0 = {
    import akka.http.scaladsl.server.Directives._

    mapInnerRoute { innerRoute =>
      extractRequest { req =>
        onSuccess(req.toStrict(marshallingTimeout)) { strictReq =>
          mapRequest(_ => strictReq) {
            innerRoute
          }
        }
      }
    }
  }
}
