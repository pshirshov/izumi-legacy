package org.bitbucket.pshirshov.izumitk.akka.http.util

import akka.http.scaladsl.server.RequestContext

/**
  */
trait RequestTransformer {
  def requestMapper: RequestContext => RequestContext = {rc => rc}
}
