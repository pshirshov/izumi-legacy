package org.bitbucket.pshirshov.izumitk.http.util

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{HttpRequest, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.http.auth.Authorizations
import org.bitbucket.pshirshov.izumitk.http.rest.HttpService

import scala.concurrent.ExecutionContext

@Singleton
class JmxProxyService @Inject()
(
  protected val system: ActorSystem
  , protected val authorizations: Authorizations
  , @Named("@jetty.port") protected val jettyJmxPort: Int
  , @Named("@jetty.interface") protected val jettyJmxHost: String
  , @Named("@auth.key") protected val apiKey: String
  , protected implicit val executionContext: ExecutionContext
  , protected implicit val materializer: Materializer
)
  extends HttpService {
  private val proxy = LoopbackProxy.create(system, jettyJmxHost, jettyJmxPort)

  override val routes: Route = pathPrefix("jmx") {
    authorizations.withFrameworkCredentials {
      cred =>
        authorize(authorizations.inFrameworkContext(cred)) {
          // TODO: mappers below are tricky, fragile and potentially unsafe
          (pathPrefix("jolokia") | pathPrefix("console")) {
            mapRequest {
              r =>
                val newUri: Uri = JmxProxyService.dropTopSegments(r, 3)
                r.copy(uri = newUri)
            } {
              mapResponse {
                res =>
                  if (res.status == StatusCodes.MovedPermanently) {
                    val withoutLocation = res.headers.filterNot(_.is(Location.lowercaseName))
                    val locationHeader = res.headers.find(_.is(Location.lowercaseName)).get
                    val uri = Uri(locationHeader.value())
                    if (uri.path.toString().startsWith("/console")) {
                      // TODO: is it safe enough? Isn't it better to use cookies?..
                      val fixedPath = uri.withPath(Uri.Path(s"/jmx/$apiKey") ++ uri.path)
                      val newHeaders = withoutLocation :+ Location(fixedPath)
                      res.copy(headers = newHeaders)
                    } else {
                      res
                    }
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

object JmxProxyService {
  def dropTopSegments(r: HttpRequest, count: Int): Uri = {
    val sourcePath = r.uri.path.toString()
    val dropped = sourcePath.split("/", -1).drop(count)
    val newPath = dropped.mkString("/")
    val newUri = r.uri.withPath(Path(s"/$newPath"))
    newUri
  }
}
