package org.bitbucket.pshirshov.izumitk.app.modules

import com.google.inject.name.{Named, Names}
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.{Config, ConfigList, ConfigObject, ConfigValueType}
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.ScalaModule

import scala.language.existentials
import scala.util.{Success, Try}

case class ConfigBinding(value: Any, clazz: Class[_])


final class ConfigExposingModule(val config: Config) extends ScalaModule with StrictLogging {
  override def configure(): Unit = {
    flatten(config.root())
      .foreach {
        case (k, v) =>
          val name = s"@$k"
          if (v != null) {
            logger.debug(s"Binding $name:${v.clazz} => ${v.value}")
            bind(v.clazz.asInstanceOf[Class[Any]])
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
    import scala.collection.JavaConverters._

    val ret = scala.collection.mutable.HashMap[String, ConfigBinding]()
    section.entrySet().asScala.foreach {
      e =>
        val key = e.getKey
        val value = e.getValue
        val uvalue = value.unwrapped()
        value.valueType() match {
          case ConfigValueType.OBJECT =>
            val subConfig = value.asInstanceOf[ConfigObject]
            ret.put(s"$key.*", ConfigBinding(subConfig.toConfig, classOf[Config]))

            // Config sections with numeric keys are lists as well as objects
            Try(section.toConfig.getList(key)) match {
              case Success(lst) =>
                ret.put(s"$key[]", ConfigBinding(lst.asInstanceOf[ConfigList], classOf[ConfigList]))
              case _ =>
            }

            flatten(subConfig).foreach {
              case (subkey, subvalue) =>
                ret.put(s"$key.$subkey", subvalue)
            }
          case ConfigValueType.LIST =>
            ret.put(s"$key[]", ConfigBinding(value.asInstanceOf[ConfigList], classOf[ConfigList]))
          case ConfigValueType.NULL =>
          case _ =>
            ret.put(s"$key", ConfigBinding(uvalue, uvalue.getClass))
        }
    }
    ret.toMap
  }


}
