package org.bitbucket.pshirshov.izumitk.failures.services

import com.codahale.metrics.MetricRegistry
import com.datastax.driver.core.Row
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.apache.commons.lang3.exception
import org.bitbucket.pshirshov.izumitk.cassandra.facade._
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import org.bitbucket.pshirshov.izumitk.util.{ExceptionUtils, SerializationUtils}

import scala.collection.JavaConverters._

@Singleton
class CassandraFailureRespository @Inject()
(
  @Named("standardMapper") protected val mapper: JacksonMapper
  , protected val query: FailureRepositoryQueries
  , protected val metrics: MetricRegistry
  , @Named("app.id") protected val productId: String
)
  extends FailureRepository {

  import query._

  override def readFailure(failureId: String): Option[RestoredFailureRecord] = {
    execute(selectFailure.bind(failureId)).asScala.map(instantiate).headOption
  }

  override protected def writeFailureRecord(id: String, failure: FailureRecord): Unit = {
    val meta = failure.causes.zipWithIndex.flatMap {
      case (t, idx) =>
        Seq(
          s"@failure[$idx].messages" -> ExceptionUtils.allMessages(t).mkString("; ")
          , s"@failure[$idx].classes" -> ExceptionUtils.allClasses(t).mkString("; ")
          , s"@failure[$idx].dump" -> ExceptionUtils.format(t)
        )
    }.toMap ++ Map("product" -> productId)

    import scala.collection.JavaConverters._

    execute(writeFailure.bind(
      mapper.writeValueAsString(failure.data.asJava)
      , mapper.writeValueAsString(meta.asJava)
      , mapper.writeValueAsString(failure.causes.map(exception.ExceptionUtils.getStackTrace).toList.asJava)
      , SerializationUtils.toByteBuffer(failure.causes)
      , id))
  }

  override def enumerate(visitor: (RestoredFailureRecord) => Unit): Unit = {
    execute(query.selectAllFailures.bind())
      .asScala
      .toIterator
      .map(instantiate)
      .foreach(visitor)
  }

  private def instantiate(row: Row): RestoredFailureRecord = {
    val data = mapper.readValue[Map[String, String]](row.getString("data"))
    val meta = mapper.readValue[Map[String, String]](row.getString("meta"))
    val stacktraces = mapper.readValue[Seq[String]](row.getString("stacktraces"))
    val exceptions = SerializationUtils.toByteArray(row.getBytes("exceptions"))

    RestoredFailureRecord(data, meta, stacktraces, exceptions, row.getString("id"))
  }
}

class FailureRepositoryQueries @Inject()
(
  protected val cassandra: CassandraContext
) extends CassandraFacade {

  private val tFailures = CTable("failures")

  override protected val ddl: Seq[CBaseStatement] = Seq(
    CTextTableStatement(CQWrite("ddl"), tFailures, ctx =>
      s"""
         | CREATE TABLE IF NOT EXISTS ${ctx.table.name} (
         |   id text,
         |   data text,
         |   meta text,
         |   stacktraces text,
         |   exceptions blob,
         |   PRIMARY KEY (id)
         | ) WITH WITH ${ctx.config.render} ;
      """.stripMargin
    )
  )

  lazy val selectFailure: CPreparedStatement = prepareQuery(CQRead("failures-get"), tFailures) {
    ctx => s"SELECT * FROM ${ctx.table.name} WHERE id = ?"
  }

  lazy val selectAllFailures: CPreparedStatement = prepareQuery(CQRead("failures-get-all"), tFailures) {
    ctx => s"SELECT * FROM ${ctx.table.name} ALLOW FILTERING"
  }

  lazy val writeFailure: CPreparedStatement = prepareQuery(CQRead("failures-put"), tFailures) {
    ctx => s"UPDATE ${ctx.table.name} SET data = ?, meta = ?, stacktraces = ?, exceptions = ? WHERE id = ?"
  }
}
