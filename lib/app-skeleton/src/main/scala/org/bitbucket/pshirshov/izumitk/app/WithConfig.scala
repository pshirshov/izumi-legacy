package org.bitbucket.pshirshov.izumitk.app

import com.typesafe.config.Config
import net.codingwell.scalaguice.{ScalaModule, ScalaPrivateModule}

/**
  */
trait WithConfig {
  val config: Config
}

trait ConfigurableModule extends ScalaModule with WithConfig

trait ConfigurablePrivateModule extends ScalaPrivateModule with WithConfig
