package org.bitbucket.pshirshov.izumitk.cassandra.facade

import com.datastax.driver.core._



sealed trait CBaseStatement {
  def meta: CMeta
}

final case class CBoundStatement(meta: CMeta, boundStatement: BoundStatement) extends CBaseStatement {
}

final case class CPreparedStatement(meta: CMeta, preparedStatement: PreparedStatement)

trait CStatement extends CBaseStatement {
  def toStatement(context: CassandraContext): Statement
}

final case class CRawStatement(meta: CMeta, statement: Statement) extends CStatement {
  override def toStatement(context: CassandraContext): Statement = statement
}

final case class CTextTableStatement(meta: CMeta, table: CTable, text: QueryContext => String) extends CStatement {
  override def toStatement(context: CassandraContext): Statement = {
    new SimpleStatement(text(QueryContext(table, context.table(table), context)))
  }
}

trait CRegularStatement extends CStatement {
  def toStatement(context: CassandraContext): RegularStatement
}

final case class CRawRegularStatement(meta: CMeta, statement: RegularStatement) extends CRegularStatement {
  override def toStatement(context: CassandraContext): RegularStatement = statement
}

