package org.bitbucket.pshirshov.izumitk.http.hal.modules

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.theoryinpractise.halbuilder5.json.JsonRepresentationWriter
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.bitbucket.pshirshov.izumitk.http.hal.serializer._


final class HalModule() extends ScalaModule {
  override def configure(): Unit = {
    bind[JacksonHalSerializer].to[JacksonHalSerializerDefaultImpl].in[Singleton]
    bind[HalSerializer].to[HalSerializerImpl].in[Singleton]
    bind[HalHooks].to[HalHooksImpl].in[Singleton]

    val _ = ScalaMultibinder.newSetBinder[HalHook](binder)
  }

  @Provides
  @Singleton
  def jsonRepresentationWriter(@Named("@json.compact") compact: Boolean): JsonRepresentationWriter = {
    val mapper = new ObjectMapper()
    if (!compact) {
      mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
      mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter())
    }
    JsonRepresentationWriter.create(mapper)
  }
}
