package org.bitbucket.pshirshov.izumitk.akka.http.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.akka.http.auth.Authorizations
import org.bitbucket.pshirshov.izumitk.akka.http.util.client.LoopbackProxy

import scala.concurrent.ExecutionContext

@Singleton
class JmxProxyService @Inject()
(
  protected val system: ActorSystem
  , protected val authorizations: Authorizations
  , @Named("@jetty.port") protected val jettyJmxPort: Int
  , @Named("@jetty.interface") protected val jettyJmxHost: String
  , protected implicit val executionContext: ExecutionContext
  , protected implicit val materializer: Materializer
)
  extends HttpService
    with StrictLogging {

  private val proxy = LoopbackProxy.create(system, jettyJmxHost, jettyJmxPort)

  override val routes: Route = prefix {
    pathPrefix("jmx") {
      authorizations.withFrameworkCredentials {
        cred =>
          authorize(authorizations.inFrameworkContext(cred)) {
            (pathPrefix("jolokia") | pathPrefix("console")) {
              mapRequest(requestMapper) {
                mapResponse {
                  res =>
                    if (res.status == StatusCodes.MovedPermanently) {
                      rewriteLocation(res)
                    } else {
                      res
                    }
                } {
                  proxy
                }
              }
            }
          }
      }
    }
  }

  protected def rewriteLocation(res: HttpResponse): HttpResponse = {
    val withoutLocation = res.headers.filterNot(_.is(Location.lowercaseName))
    val locationHeader = res.headers.find(_.is(Location.lowercaseName)).get
    val uri = Uri(locationHeader.value())

    val fixedPath = uri.withPath(Uri.Path(s"/$defaultPrefix/jmx") ++ uri.path)
    val newHeaders = withoutLocation :+ Location(fixedPath)
    res.copy(headers = newHeaders)
  }

  protected def prefix: Directive0 = {
    pathPrefix(defaultPrefix)
  }

  protected def defaultPrefix: String = "diag"

  protected def requestMapper(r: HttpRequest): HttpRequest = {
    val newUri: Uri = JmxProxyService.dropTopSegments(r, 3)
    r.copy(uri = newUri)
  }
}

object JmxProxyService {
  def dropTopSegments(r: HttpRequest, count: Int): Uri = {
    val sourcePath = r.uri.path.toString()
    val dropped = sourcePath.split("/", -1).drop(count)
    val newPath = dropped.mkString("/")
    val newUri = r.uri.withPath(Path(s"/$newPath"))
    newUri
  }
}
