package org.bitbucket.pshirshov.izumitk.akka.http.auth.model

/**
  */
trait AuthorizationContext {}

object AuthorizationContext {
  case object Framework extends AuthorizationContext
}
