package org.bitbucket.pshirshov.izumitk.modularity.model

import com.typesafe.config.Config

/**
  */
protected[modularity] case class PluginsConfig(
                          enabled: Boolean
                          , deactivated: Set[String]
                          , activated: Set[String]
                          , targets: Config
                        )
