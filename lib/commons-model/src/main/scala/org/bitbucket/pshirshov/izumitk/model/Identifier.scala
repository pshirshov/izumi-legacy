package org.bitbucket.pshirshov.izumitk.model

/**
  */
trait Identifier extends Ordered[Identifier] {
  def asString: String

  override def compare(that: Identifier): Int = this.asString.compare(that.asString)
}
