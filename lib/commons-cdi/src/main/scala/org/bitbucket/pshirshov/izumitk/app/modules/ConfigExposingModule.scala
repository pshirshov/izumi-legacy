package org.bitbucket.pshirshov.izumitk.app.modules

import com.google.inject.name.{Named, Names}
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.{Config, ConfigList, ConfigObject, ConfigValueType}
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule

case class ConfigBinding(value: Any, clazz: Class[Any])

final class ConfigExposingModule(val config: Config) extends ScalaModule with StrictLogging {
  override def configure(): Unit = {
    flatten(config.root())
      .foreach {
      case (k, v) =>
        val name = s"@$k"
        if (v != null) {
          logger.debug(s"Binding $name:${v.clazz} => ${v.value}")
          bind(v.clazz)
            .annotatedWith(Names.named(name))
            .toInstance(v.value)
        }
    }
  }

  @Provides
  @Singleton
  @Named("app.config")
  def appConfig: Config = config

  private def flatten(section: ConfigObject): Map[String, ConfigBinding] = {
    import scala.collection.JavaConversions._

    val ret = scala.collection.mutable.HashMap[String, ConfigBinding]()
    section.entrySet().foreach {
      e =>
        val key = e.getKey
        val value = e.getValue
        val uvalue = value.unwrapped()
        value.valueType() match {
          case ConfigValueType.OBJECT =>
            ret.put(s"$key.*", ConfigBinding(value.asInstanceOf[ConfigObject].toConfig, classOf[Config].asInstanceOf[Class[Any]]))
            flatten(value.asInstanceOf[ConfigObject]).foreach {
              case (subkey, subvalue) =>
                ret.put(s"$key.$subkey", subvalue)
            }
          case ConfigValueType.LIST =>
            ret.put(s"$key[]", ConfigBinding(value.asInstanceOf[ConfigList], classOf[ConfigList].asInstanceOf[Class[Any]]))
          case ConfigValueType.NULL =>
          case _ =>
            ret.put(s"$key", ConfigBinding(uvalue, uvalue.getClass.asInstanceOf[Class[Any]]))
        }
    }
    ret.toMap
  }
}
