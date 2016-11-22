package org.bitbucket.pshirshov.izumitk.akka.http.services

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import org.bitbucket.pshirshov.izumitk.akka.http.auth.Authorizations
import org.bitbucket.pshirshov.izumitk.akka.http.util.logging.WSLogger

import scala.concurrent.ExecutionContext

@Singleton
class WebDiagnostics @Inject()
(
  protected val system: ActorSystem
  , protected val authorizations: Authorizations
  , @Named("app.config") protected val appConfig: Config
  , protected implicit val executionContext: ExecutionContext
  , protected implicit val materializer: Materializer
)
  extends HttpService {

  override val routes: Route = pathPrefix("diag") {
    authorizations.withFrameworkCredentials {
      cred =>
        authorize(authorizations.inFrameworkContext(cred)) {
          (get & path("config") & pathEndOrSingleSlash) {
            complete(appConfig.root().render())
          } ~ (get & path("log")) {
            handleWebSocketMessagesForProtocol(WSLogger.createLoggerFlow(), "ws-logger")
          }
        }
    }
  }
}
