package org.bitbucket.pshirshov.izumitk.cassandra.facade

import com.datastax.driver.core.ConsistencyLevel

case class CassandraConfig(values: Map[String, String]) {
  def render: String = {
    values.map(p => s" ${p._1} = ${p._2} ").mkString(" AND ")
  }
}


case class CQueryConfig(consistencyLevel: Option[ConsistencyLevel] = None)

case class CTable(keyspaceName: CKeyspace, name: String) {
  def fqName: String = s"${keyspaceName.name}.$name"
}

case class CKeyspaceId(id: String)
case class CKeyspace(name: String)

case class QueryContext(cTable: CTable, config: CassandraConfig, cassandra: CassandraContext) {
  def table: String = cTable.fqName
}

