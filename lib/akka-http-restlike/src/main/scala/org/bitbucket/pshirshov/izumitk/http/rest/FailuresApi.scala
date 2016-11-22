package org.bitbucket.pshirshov.izumitk.http.rest

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.akka.http.auth.Authorizations
import org.bitbucket.pshirshov.izumitk.akka.http.services.HttpService
import org.bitbucket.pshirshov.izumitk.failures.services.FailureRepository
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import org.scalactic.Good

@Singleton
class FailuresApi @Inject()
(
  override protected val apiPolicy: JsonAPIPolicy
  , @Named("standardMapper") protected val mapper: JacksonMapper
  , protected val failureRepository: FailureRepository
  , protected val authorizations: Authorizations
) extends HttpService
  with JsonAPI {

  override val routes: server.Route =
    pathPrefix("failures") {
      authorizations.withFrameworkCredentials {
        cred =>
          authorize(authorizations.inFrameworkContext(cred)) {
            (get & path(Segment)) {
              failureId =>
                completeJson {
                  val failure = failureRepository.readFailure(failureId) /*.map {
                    f =>
                      import scala.collection.JavaConverters._
                      val output = mapper.valueToTree[ObjectNode](f.copy(causes = Vector()))
                      val causes = JsonNodeFactory.instance.arrayNode()
                      causes.addAll(f.causes.map(t => new TextNode(ExceptionUtils.getStackTrace(t))))
                      output.set("causes", causes)
                      output
                  }*/
                  JsonAPI.Result(Good(failure), endpointName = "private-failures")
                }
            }
          }
      }
    }
}
