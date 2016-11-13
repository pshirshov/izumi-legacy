package org.bitbucket.pshirshov.izumitk.test

import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.sift.Discriminator
import ch.qos.logback.core.spi.ContextAwareBase

@ExposedTestScope
class GlobalDiscriminator extends ContextAwareBase with Discriminator[LoggingEvent] {
  def getDiscriminatingValue(event: LoggingEvent) = {
    GlobalDiscriminator.value
  }

  def getKey: String = "test-name"

  override def stop(): Unit = {}

  override def isStarted: Boolean = true

  override def start(): Unit = {}
}

object GlobalDiscriminator {
  @volatile
  private var value: String = null

  def setValue(v: String) = {
    value = v
  }
}
