package org.bitbucket.pshirshov.izumitk.cassandra.facade

import com.codahale.metrics.Timer
import com.codahale.metrics.Timer.Context
import com.datastax.driver.core._
import com.google.common.util.concurrent.{FutureCallback, Futures}
import com.typesafe.scalalogging.StrictLogging
import org.bitbucket.pshirshov.izumitk.cdi.Initializable

import scala.collection.convert.{DecorateAsJava, DecorateAsScala}
import scala.concurrent.{Future, Promise}


trait WithCassandraFacade
  extends DecorateAsJava with DecorateAsScala {
  protected def facade: CassandraFacade

  implicit class Binder(query: CPreparedStatement) {
    def bind(args: AnyRef*): CBoundStatement = {
      facade.bind(query, args: _*)
    }
  }

  def execute(query: CPreparedStatement, args: AnyRef*): ResultSet = {
    facade.execute(facade.bind(query, args: _*))
  }
}

trait CassandraFacade
  extends Initializable
    with WithCassandraFacade
    with StrictLogging {

  protected def cassandra: CassandraContext

  protected def ddl: Seq[CBaseStatement]
  
  override protected def facade: CassandraFacade = this

  protected def defaultKeyspaceId: CKeyspaceId = CKeyspaceId("default")
  protected def inDefaultKeyspace(name: String): CTable = CTable(aliasToName(defaultKeyspaceId), name)
  protected def aliasToName(ksId: CKeyspaceId): CKeyspace = cassandra.keyspaceAliases(ksId)

  def createTables(): Unit = {
    classOf[CassandraFacade].synchronized {
      ddl.foreach(s => execute(s))
    }
  }

  abstract override def init(): Unit = {
    super.init()
    createTables()
  }

  def prepareQuery(meta: CMeta, table: CTable)(text: QueryContext => String): CPreparedStatement = {
    val qctx = QueryContext(table, cassandra.config(table), cassandra)
    val statement = text(qctx)
    val prepared = cassandra.psCache.get(statement)
    CPreparedStatement(meta, prepared)
  }

  def prepareStatement(meta: CMeta, table: CTable)(statement: QueryContext => CRegularStatement): CPreparedStatement = {
    val qctx = QueryContext(table, cassandra.config(table), cassandra)
    val cs = statement(qctx)
    val prepared = cassandra.session.prepare(cs.toStatement(cassandra))
    CPreparedStatement(meta, prepared)
  }

  def bind(query: CPreparedStatement, values: AnyRef*): CBoundStatement = {
    CBoundStatement(query.meta, query.preparedStatement.bind(values: _*))
  }

  def execute(statement: => CBaseStatement): ResultSet = {
    val timerName = statement.meta.name
    logger.trace(s"C* Query: ${log(statement).mkString(";")} [$timerName]")

    val timer = cassandra.metrics.timer(s"${cassandra.productId.id}-c-$timerName")
    val context = timer.time()
    try {
      statement match {
        case s: CStatement =>
          cassandra.session.execute(configure(s.meta, s.toStatement(cassandra)))
        case s: CBoundStatement =>
          cassandra.session.execute(configure(s.meta, s.boundStatement))
      }
    } finally {
      context.stop()
      logger.trace(s"$timerName: ${timer.getSnapshot.getValues.lastOption}")
    }
  }

  def executeAsync(statement: => CBaseStatement): Future[ResultSet] = {
    val timerName = statement.meta.name

    logger.trace(s"C* Query: ${log(statement).mkString(";")} [$timerName]")

    val timer = cassandra.metrics.timer(s"${cassandra.productId.id}-ca-$timerName")
    convertFuture(timer, timer.time(), timerName) {
      statement match {
        case s: CStatement =>
          cassandra.session.executeAsync(configure(s.meta, s.toStatement(cassandra)))
        case s: CBoundStatement =>
          cassandra.session.executeAsync(configure(s.meta, s.boundStatement))
      }
    }
  }

  private def configure(meta: CMeta, s: Statement): Statement = {
    val queryConfig = cassandra.querySettings.getOrElse(meta.name, {
      cassandra.querySettings.getOrElse(s"default-${meta.tag}", cassandra.defaultQueryConfig)
    })


    queryConfig.consistencyLevel match {
      case Some(cl) =>
        s.setConsistencyLevel(cl)

      case _ =>
        s
    }

    queryConfig.serialConsistencyLevel match {
      case Some(cl) =>
        s.setSerialConsistencyLevel(cl)
      case _ =>
        s
    }

  }

  private def convertFuture(timer: Timer, context: Context, timerName: String)(lf: => ResultSetFuture): Future[ResultSet] = {
    val p = Promise[ResultSet]()
    Futures.addCallback(lf, new FutureCallback[ResultSet] {
      def onFailure(t: Throwable): Unit = {
        p.failure(t)
      }

      def onSuccess(result: ResultSet): Unit = {
        try {
          p.success(result)
        } finally {
          context.stop()
          logger.trace(s"$timerName: ${timer.getSnapshot.getValues.lastOption}")
        }

      }

    })
    p.future
  }

  private def log(statement: CBaseStatement): Seq[String] = {
    statement match {
      case s: CStatement =>
        log(s.toStatement(cassandra))
      case s: CBoundStatement =>
        log(s.boundStatement)
    }
  }

  private def log(statement: Statement): Seq[String] = {
    statement match {
      case s: SimpleStatement =>
        Seq(s.getQueryString)
      case s: BoundStatement =>
        Seq(s.preparedStatement().getQueryString)
      case s: PreparedStatement =>
        Seq(s.getQueryString)
      case s: BatchStatement =>
        s.getStatements.asScala.flatMap {
          subs =>
            log(subs)
        }.toSeq
      case o =>
        throw new IllegalArgumentException(s"Don't know how to print $o")
    }
  }
}
