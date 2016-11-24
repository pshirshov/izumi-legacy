package org.bitbucket.pshirshov.izumitk.http.rest.serialization

import com.fasterxml.jackson.databind.JsonNode

/**
  */
case class RestData[T](data: T, meta: Option[Map[String, JsonNode]] = None, code: Option[Int] = None)
