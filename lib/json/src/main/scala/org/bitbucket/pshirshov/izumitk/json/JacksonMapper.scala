package org.bitbucket.pshirshov.izumitk.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

class JacksonMapper extends ObjectMapper with ScalaObjectMapper {
  override def _checkInvalidCopy(exp: Class[_]): Unit = ()
}
