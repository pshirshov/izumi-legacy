package org.bitbucket.pshirshov.izumitk.cdi.config

import java.lang.reflect.Field

import com.google.inject.MembersInjector
import com.typesafe.config.Config

class HoconMembersInjector[T](field: Field, ann: Conf, config: Config) extends MembersInjector[T] {
  override def injectMembers(t: T): Unit = {
    try {
      field.setAccessible(true)
      field.get(t).asInstanceOf[C[AnyRef]].value.set(config.getAnyRef(ann.value()))
      field.setAccessible(false)
    } catch {
      case e: IllegalAccessException =>
        throw new RuntimeException(e)
    }
  }
}
