package org.bitbucket.pshirshov.izumitk.http.hal.modules

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import com.theoryinpractise.halbuilder.standard.StandardRepresentationFactory
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.http.hal.{HalSerializer, HalSerializerImpl}


final class HalModule() extends ScalaModule {
  override def configure(): Unit = {
    bind[HalSerializer].to[HalSerializerImpl].in[Singleton]
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
