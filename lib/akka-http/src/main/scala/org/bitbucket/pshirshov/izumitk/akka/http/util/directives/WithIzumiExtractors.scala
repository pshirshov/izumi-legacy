package org.bitbucket.pshirshov.izumitk.akka.http.util.directives

import java.util.UUID

import akka.http.scaladsl.server.{PathMatcher, PathMatcher1}

trait WithIzumiExtractors {
  protected def simpleUuidExtractor[T](prefix: String, constructor: UUID => T): PathMatcher1[T] = {
    simpleIdExtractor(prefix
      , "[\\da-fA-F]{8}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{4}-[\\da-fA-F]{12}"
      , constructor.compose(UUID.fromString)
    )
  }

  protected def simpleIdExtractor[T](prefix: String, regex: String, constructor: String => T): PathMatcher1[T] = {
    PathMatcher(s"$prefix\\:$regex".r) flatMap {
      string =>
        try {
          val uuid = string.substring(prefix.length + 1)
          Some(constructor(uuid))
        } catch {
          case _: IllegalArgumentException ⇒ None
        }
    }
  }
}
