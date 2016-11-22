package org.bitbucket.pshirshov.izumitk.cassandra.util


import com.codahale.metrics.MetricRegistry
import com.datastax.driver.core._
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.cassandra.PSCache

import scala.collection.JavaConverters._


trait CassandraQueries extends StrictLogging {
  protected val psCache: PSCache

  protected val session: Session
  protected val keyspace: String
  protected val productId: String

  protected val metrics: MetricRegistry

  protected def ddl: Seq[String]

  def createTables(): Unit = {
    ddl.foreach {
      q =>
        session.execute(q)
    }
  }

  createTables()

  def execute(statement: Statement, timerName: String): ResultSet = {
    logger.trace(s"C* Query: ${log(statement).mkString(";")} [$timerName]")

    val timer = metrics.timer(s"$productId-c-$timerName")
    val context = timer.time()
    try {
      session.execute(statement)
    } finally {
      context.stop()
      logger.trace(s"$timerName: ${timer.getSnapshot.getValues.lastOption}")
    }
  }

  private def log(statement: Statement): Seq[String] = {
    statement match {
      case s: BoundStatement =>
        Seq(s.preparedStatement().getQueryString)
      case s: PreparedStatement =>
        Seq(s.getQueryString)
      case s: BatchStatement =>
        s.getStatements.asScala.flatMap {
          subs =>
            log(subs)
        }.toSeq
      case _ =>
        throw new IllegalArgumentException()
    }

  }
}
