package org.bitbucket.pshirshov.izumitk

import com.typesafe.config.{Config, ConfigFactory}
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope

@ExposedTestScope
object TestConfig {
  def references(sections: String*): Config = {
    val out = sections.map(reference).toList match {
      case head :: Nil =>
        head
      case configs@head :: tail =>
        configs.reduce((c1, c2) => c1.withFallback(c2))
      case _ =>
        throw new IllegalStateException()
    }

    out
      .withFallback(ConfigFactory.defaultReference())
      .resolve()
  }

  private def reference(section: String): Config = {
    val filename = s"$section-reference.conf"

    val config = ConfigFactory.parseResources(filename)

    if (config.isEmpty) {
      throw new IllegalStateException(s"Empty reference: `$section`")
    }
    import scala.collection.JavaConversions._

    import scala.collection.JavaConverters._
    val unwrapped = config.root().unwrapped().map {
      case (k, v) =>
        (s"$section.$k", v)
    }.toMap.asJava

    ConfigFactory.parseMap(unwrapped)
  }
}
