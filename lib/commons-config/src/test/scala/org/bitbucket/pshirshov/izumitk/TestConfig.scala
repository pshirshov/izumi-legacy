package org.bitbucket.pshirshov.izumitk

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope


@ExposedTestScope
object TestConfig extends StrictLogging {
  import scala.collection.JavaConverters._

  case class TestConfigSection(resourceName: String, alias: String)

  def references(sections: TestConfigSection*): Config = {
    val out = sections.map(reference).toList match {
      case Nil =>
        ConfigFactory.empty()
      case head :: Nil =>
        head
      case configs@head :: tail =>
        configs.reduce((c1, c2) => c1.withFallback(c2))
      case _ =>
        throw new IllegalStateException()
    }


    val config = ConfigFactory.systemProperties()
      .withFallback(out)
      .withFallback(ConfigFactory.defaultReference())
      .resolve()
    logger.trace(s"Test config loaded: $sections ==> $config")
    config
  }

  private def reference(section: TestConfigSection): Config = {
    val filename = s"${section.resourceName}-reference.conf"

    val config = ConfigFactory.parseResources(filename)

    if (config.isEmpty) {
      throw new IllegalStateException(s"Empty reference: `$section`")
    }

    val unwrapped = config.root().unwrapped().asScala.map {
      case (k, v) =>
        (s"${section.alias}.$k", v)
    }.toMap.asJava

    ConfigFactory.parseMap(unwrapped)
  }
}
