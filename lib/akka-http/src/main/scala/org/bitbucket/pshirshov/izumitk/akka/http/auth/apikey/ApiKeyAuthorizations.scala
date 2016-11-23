package org.bitbucket.pshirshov.izumitk.akka.http.auth.apikey

import akka.http.scaladsl.model.HttpHeader
import com.google.inject._
import com.google.inject.name.Named


@Singleton
class ApiKeyAuthorizations @Inject()
(
  @Named("@auth.key") protected val apiKey: String
  , @Named("@rest.api-key-schema") override protected val keySchemaName: String
)
  extends ApiKeySupport
    with ApiKeyDirectives {

  override protected def extractApiKey(): (HttpHeader) => Option[String] = extractKey(keySchemaName)

  def authorizedApiKey(key: String): Boolean = {
    apiKey == key
  }
}

