package org.bitbucket.pshirshov.izumitk.http

import akka.http.scaladsl.testkit.RouteTest
import akka.http.scaladsl.unmarshalling._
import com.fasterxml.jackson.databind.node.ObjectNode
import org.bitbucket.pshirshov.izumitk.http.hal.decoder.HalDecoder
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope

/**
  */
@ExposedTestScope
trait WithHalDecoder {
  this: RouteTest =>

  import scala.reflect.runtime.universe._

  def halResponseAs[T: TypeTag : Manifest](implicit unm: FromResponseUnmarshaller[ObjectNode], decoder: HalDecoder): T = {
    decoder.readHal[T](responseAs[ObjectNode])
  }
}
