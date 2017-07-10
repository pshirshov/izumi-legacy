package org.bitbucket.pshirshov.izumitk.modules

import com.google.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.geoip.services.{GeoipService, GeoipServiceImpl}

class GeoDataModule() extends ScalaModule {
  override def configure(): Unit = {
    bind[GeoipService].to[GeoipServiceImpl].in[Singleton]
  }
}