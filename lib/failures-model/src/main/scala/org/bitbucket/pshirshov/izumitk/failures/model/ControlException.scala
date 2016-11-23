package org.bitbucket.pshirshov.izumitk.failures.model

/**
  * This trait represents an exceptions which is used to implement alternate control flows,
  * but doesn't mean that something bad happened. For example, it may be used to signal something
  * like HTTP NOT FOUND event.
  */
trait ControlException
  extends ServiceFailure {
  this: RuntimeException =>

}
