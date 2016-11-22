package org.bitbucket.pshirshov.izumitk.failures.services

import com.codahale.metrics.MetricRegistry
import com.datastax.driver.core.{ConsistencyLevel, Row, Session}
import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named
import org.bitbucket.pshirshov.izumitk.cassandra.PSCache
import org.bitbucket.pshirshov.izumitk.cassandra.util.CassandraQueries
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import org.bitbucket.pshirshov.izumitk.services.{FailureRecord, FailureRepository, RestoredFailureRecord}
import org.bitbucket.pshirshov.izumitk.util.{ExceptionUtils, SerializationUtils}
import org.apache.commons.lang3.exception

import scala.collection.JavaConversions._

@Singleton
class CassandraFailureRespository @Inject()
(
  @Named("standardMapper") protected val mapper: JacksonMapper
  , protected val query: FailureRepositoryQueries
  , protected val metrics: MetricRegistry
  , @Named("app.id") protected val productId: String
)
  extends FailureRepository {

  override def readFailure(failureId: String): Option[RestoredFailureRecord] = {
    query.execute(query.selectFailure.bind(failureId), "failure-get").map(instantiate).headOption
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

    query.execute(query.writeFailure.bind(
      mapper.writeValueAsString(failure.data.asJava)
      , mapper.writeValueAsString(meta.asJava)
      , mapper.writeValueAsString(failure.causes.map(exception.ExceptionUtils.getStackTrace).toList.asJava)
      , SerializationUtils.toByteBuffer(failure.causes)
      , id), "failure-put")
  }

  override def enumerate(visitor: (RestoredFailureRecord) => Unit): Unit = {
    query.execute(query.selectAllFailures.bind(), "failure-enumerate")
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
  protected val psCache: PSCache
  , protected val session: Session
  , protected val metrics: MetricRegistry
  , @Named("cassandra.keyspace") protected val keyspace: String
  , @Named("app.id") override protected val productId: String
) extends CassandraQueries {

  override def ddl = Seq(
  """
    | CREATE TABLE IF NOT EXISTS failures02 (
    |   id text,
    |   data text,
    |   meta text,
    |   stacktraces text,
    |   exceptions blob,
    |   PRIMARY KEY (id)
    | ) WITH
    | compaction = {'class': 'LeveledCompactionStrategy'}
    | AND
    | compression = { 'sstable_compression' : 'SnappyCompressor' };
  """.
  stripMargin
  )

  val selectFailure = {
    val stmt = psCache.get("SELECT * FROM failures02 WHERE id = ?")
    stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
    stmt
  }

  val selectAllFailures = {
    val stmt = psCache.get("SELECT * FROM failures02 ALLOW FILTERING")
    stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
    stmt
  }

  val writeFailure = {
    val stmt = psCache.get("UPDATE failures02 SET data = ?, meta = ?, stacktraces = ?, exceptions = ? WHERE id = ?")
    stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
    stmt
  }
}
