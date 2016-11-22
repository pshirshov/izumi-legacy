package org.bitbucket.pshirshov.izumitk.akka.http.auth.impl

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import com.google.inject._
import com.google.inject.name.Named


@Singleton
class ApiKeyAuthorizations @Inject()(
                                     @Named("@auth.key") protected val apiKey: String
                                     , @Named("@rest.api-key-response-header") keyHeaderName: String
                                   ) extends KeyAuthorizations {
  def withApiKey: server.Directive1[String] = headerValue(extractApiKey())

  def authorizedApiKey(key: String): Boolean = {
    apiKey == key
  }

  def extractApiKey(): HttpHeader => Option[String] = {
    extractKey(keyHeaderName)
  }
}

