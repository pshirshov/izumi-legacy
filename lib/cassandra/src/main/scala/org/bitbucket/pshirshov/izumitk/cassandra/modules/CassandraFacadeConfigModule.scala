package org.bitbucket.pshirshov.izumitk.cassandra.modules

import com.datastax.driver.core._
import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.{Config, ConfigObject, ConfigValue}
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.cassandra.facade.{CQueryConfig, CassandraConfig}

import scala.collection.JavaConverters._


final class CassandraFacadeConfigModule() extends ScalaModule {
  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  def queryConfigs(@Named("@cassandra.settings.queries.*") config: Config): Map[String, CQueryConfig] = {
    val values = dereference(config)

    values.map {
      case (name, cfg) =>
        name -> CQueryConfig(Option(cfg.getString("consistency")).map(ConsistencyLevel.valueOf))
    }
  }

  @Provides
  @Singleton
  def tableConfigs(@Named("@cassandra.settings.tables.*") config: Config): Map[String, CassandraConfig] = {
    val values = dereference(config)

    values.map {
      case (name, cfg) =>
        name -> CassandraConfig(cfg.root().unwrapped().asScala.toMap.asInstanceOf[Map[String, String]])
    }
  }

  private def dereference(config: Config): Map[String, Config] = {

    val values = config.root()

    @scala.annotation.tailrec
    def deref(value: ConfigValue): Config = value match {
      case c: ConfigObject =>
        c.toConfig

      case o =>
        o.unwrapped() match {
          case r: String =>
            if (r.startsWith("@")) {
              deref(config.getConfig(r.substring(1)).root())
            } else {
              throw new IllegalArgumentException(s"Reference must be prefixed with @: $r")
            }

          case _ =>
            throw new IllegalArgumentException(s"Unsupported: $o, ${o.getClass}")
        }
    }

    values.asScala.mapValues(deref).toMap
  }
}
