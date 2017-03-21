package org.bitbucket.pshirshov.izumitk.cassandra.facade

import com.codahale.metrics.MetricRegistry
import com.datastax.driver.core._
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.cassandra.PSCache
import org.bitbucket.pshirshov.izumitk.cluster.model.AppId


@Singleton
case class CassandraContext @Inject()
(
  querySettings: Map[String, CQueryConfig]
  , tableSettings: Map[String, CassandraConfig]
  , @Named("app.id") productId: AppId
  , metrics: MetricRegistry
  , session: Session
  , psCache: PSCache
  , @Named("cassandra.keyspaces") keyspaceAliases: Map[CKeyspaceId, CKeyspace]
) {
  val defaultQueryConfig: CQueryConfig = querySettings("default")

  def config(t: CTable): CassandraConfig = tableSettings.get(t.fqName)
    .orElse(tableSettings.get(t.name))
    .getOrElse(tableSettings("default"))
}
