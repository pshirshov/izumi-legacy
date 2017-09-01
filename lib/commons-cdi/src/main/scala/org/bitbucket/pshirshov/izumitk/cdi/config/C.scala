package org.bitbucket.pshirshov.izumitk.cdi.config

import java.util.concurrent.atomic.AtomicReference

import com.google.inject.Inject

class C[T] @Inject() () {
  protected[config] val value: AtomicReference[T] = new AtomicReference[T]()

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


