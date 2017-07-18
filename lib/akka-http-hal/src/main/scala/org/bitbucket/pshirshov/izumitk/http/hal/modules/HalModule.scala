package org.bitbucket.pshirshov.izumitk.http.hal.modules

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import com.theoryinpractise.halbuilder.standard.StandardRepresentationFactory
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.bitbucket.pshirshov.izumitk.http.hal.serializer._


final class HalModule() extends ScalaModule {
  override def configure(): Unit = {
    bind[HalSerializer].to[HalSerializerImpl].in[Singleton]
    bind[HalHooks].to[HalHooksImpl].in[Singleton]

    ScalaMultibinder.newSetBinder[HalHook](binder)
  }

  @Provides
  @Singleton
  def representationFactory(@Named("@json.compact") compact: Boolean): RepresentationFactory = {
    val f = new StandardRepresentationFactory()
    if (compact) {
      f
    } else {
      f.withFlag(RepresentationFactory.PRETTY_PRINT)
    }
  }


}
