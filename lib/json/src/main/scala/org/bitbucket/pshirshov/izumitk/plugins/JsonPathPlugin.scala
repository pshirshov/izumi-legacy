package org.bitbucket.pshirshov.izumitk.plugins

import com.google.inject.{Provides, Singleton}
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import org.bitbucket.pshirshov.izumitk.cdi.GuicePlugin

class JsonPathPlugin extends GuicePlugin {

  override def configure(): Unit = {
  }

  @Singleton
  @Provides
  def jsonPathConfiguration: Configuration = {
    Configuration.builder
      .mappingProvider(new JacksonMappingProvider)
      .jsonProvider(new JacksonJsonNodeJsonProvider)
      .build
  }
}
