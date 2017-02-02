package org.bitbucket.pshirshov.izumitk.akka.http.auth

import akka.http.scaladsl.server.{Directive0, RequestContext}

import scala.reflect.ClassTag

trait Credentials {}

trait Authorizations {
  type Cred <: Credentials

  def genericAuthorize[T:ClassTag]: Directive0

  protected val forbidden: PartialFunction[Cred, RequestContext => Boolean] = {
    case _ =>
      _: RequestContext =>
        false
  }
}

