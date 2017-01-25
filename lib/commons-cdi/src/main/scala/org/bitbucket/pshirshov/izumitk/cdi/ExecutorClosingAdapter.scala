package org.bitbucket.pshirshov.izumitk.cdi

import java.util.concurrent.{ExecutorService, TimeUnit}

sealed case class ExecutorClosingAdapter(executor: ExecutorService) extends AutoCloseable {
  override def close(): Unit = {
    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)
  }
}
