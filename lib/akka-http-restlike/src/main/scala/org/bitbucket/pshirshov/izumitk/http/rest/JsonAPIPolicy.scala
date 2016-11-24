package org.bitbucket.pshirshov.izumitk.http.rest

import akka.http.scaladsl.model.HttpHeader
import com.fasterxml.jackson.databind.JsonNode
import org.bitbucket.pshirshov.izumitk.akka.http.util.APIPolicy
import org.bitbucket.pshirshov.izumitk.failures.model.ServiceFailure
import org.scalactic.{Every, Or}


trait JsonAPIPolicy extends APIPolicy {
  def formatResponse[R: Manifest](transformedOutput: Or[JsonAPI.Response, Every[ServiceFailure]]): (JsonNode, collection.immutable.Seq[HttpHeader])
}
