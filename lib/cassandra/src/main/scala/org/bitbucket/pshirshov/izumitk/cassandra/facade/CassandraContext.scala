package org.bitbucket.pshirshov.izumitk.cassandra.facade

import com.codahale.metrics.MetricRegistry
import com.datastax.driver.core._
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.cassandra.PSCache
import org.bitbucket.pshirshov.izumitk.model.cluster.AppId


@Singleton
case class CassandraContext @Inject()
(
  @Named("cassandra.keyspace") keyspace: String
  , @Named("app.id") productId: AppId
  , tableSettings: Map[CTable, CassandraConfig]
  , querySettings: Map[String, CQueryConfig]
  , metrics: MetricRegistry
  , session: Session
  , psCache: PSCache
) {
  val defaultQueryConfig: CQueryConfig = querySettings("default")
  val defaultTableSettings: CassandraConfig = tableSettings(CTable("default"))

  def table(t: CTable): CassandraConfig = tableSettings.getOrElse(t, defaultTableSettings)
}
