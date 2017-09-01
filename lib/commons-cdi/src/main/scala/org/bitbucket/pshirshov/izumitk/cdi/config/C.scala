package org.bitbucket.pshirshov.izumitk.cdi.config

import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicReference

import com.google.inject.spi.{TypeEncounter, TypeListener}
import com.google.inject.{Inject, TypeLiteral}
import com.typesafe.config.Config

class C[T] @Inject() () {
  protected val value: AtomicReference[T] = new AtomicReference[T]()

  def apply(): T = {
    Option(value.get()) match {
      case Some(v) =>
        v
      case None =>
        throw new IllegalStateException(s"$this was not initialized")
    }
  }

  override def toString: String = apply().toString
}


