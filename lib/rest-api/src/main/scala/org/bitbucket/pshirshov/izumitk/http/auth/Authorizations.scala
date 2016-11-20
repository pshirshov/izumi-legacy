package org.bitbucket.pshirshov.izumitk.http.auth

import akka.http.scaladsl.server
import akka.http.scaladsl.server.RequestContext

trait Credentials {}

trait AuthorizationContext {}

object AuthorizationContext {
  case object Framework extends AuthorizationContext
}

trait Authorizations {
  type Cred <: Credentials

  protected def contextAuthorization(context: AuthorizationContext): (Cred) => ((RequestContext) => Boolean)

  protected val forbidden: PartialFunction[Cred, RequestContext => Boolean] = {
    case _: Cred =>
      _: RequestContext =>
        false
  }

  def inFrameworkContext(credentials: Cred): (RequestContext) => Boolean = {
    contextAuthorization(AuthorizationContext.Framework)(credentials)
  }

  def withFrameworkCredentials: server.Directive1[Cred]
}
