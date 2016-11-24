package org.bitbucket.pshirshov.izumitk.akka.http.util

import akka.http.scaladsl.server.RequestContext
import com.google.inject.{Inject, Singleton}

/**
  */
trait RequestTransformer {
  def requestMapper: RequestContext => RequestContext
}


@Singleton
class NullRequestTransformer @Inject()() extends RequestTransformer{
  override def requestMapper: RequestContext => RequestContext = {rc => rc}
}
