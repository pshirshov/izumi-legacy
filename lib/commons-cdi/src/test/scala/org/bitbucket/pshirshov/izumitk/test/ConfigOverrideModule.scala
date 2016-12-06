package org.bitbucket.pshirshov.izumitk.test

import com.google.inject.Key
import com.google.inject.name.Names
import net.codingwell.scalaguice.ScalaModule


@ExposedTestScope
class ConfigOverrideModule(overrides: Map[String, AnyRef]) extends ScalaModule {
  def this(elems: (String, AnyRef)*) = this(Map(elems: _*))

  override def configure(): Unit = {
    overrides.foreach {
      case (k, v) =>
        bind(Key.get(v.getClass.asInstanceOf[Class[AnyRef]], Names.named(k))).toInstance(v)
    }
  }
}
