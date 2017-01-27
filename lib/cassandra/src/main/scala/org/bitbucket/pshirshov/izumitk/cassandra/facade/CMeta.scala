package org.bitbucket.pshirshov.izumitk.cassandra.facade

trait CMeta {
  def name: String
  def tag: String
}

case class CQRead(name: String) extends CMeta {
  override def tag: String = "read"
}

case class CQWrite(name: String) extends CMeta {
  override def tag: String = "write"
}
