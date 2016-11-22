package org.bitbucket.pshirshov.izumitk.akka.http.auth

/**
  */
trait AuthorizationContext {}

object AuthorizationContext {
  case object Framework extends AuthorizationContext
}