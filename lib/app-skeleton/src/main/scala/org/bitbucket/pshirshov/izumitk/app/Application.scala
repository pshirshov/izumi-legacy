package org.bitbucket.pshirshov.izumitk.app

import scala.concurrent.Future

trait Application[T] extends AutoCloseable {
  def run(): Future[T]
  def shutdown(): Unit

  override final def close(): Unit = shutdown()
}


