package org.bitbucket.pshirshov.izumitk.app.modules

import com.google.inject.matcher.Matchers
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.cdi.config.ConfigProviderTypeListener

class ConfigProviderInjectionModule(config: Config) extends ScalaModule {
  override def configure(): Unit = {
    bindListener(Matchers.any(), new ConfigProviderTypeListener(config))
  }
}
