package org.bitbucket.pshirshov.izumitk.app.util

import sun.misc.{Signal, SignalHandler}

/**
  */
abstract class SysSignals {
  def installHandlers(signals: String*): Unit = {
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = onShutdown()
    })

    signals.foreach {
      s =>
        Signal.handle(new Signal(s), new SignalHandler() {
          override def handle(signal: Signal) = onSignal(signal)
        })
    }
  }

  protected def onShutdown(): Unit

  protected def onSignal(signal: Signal): Unit
}
