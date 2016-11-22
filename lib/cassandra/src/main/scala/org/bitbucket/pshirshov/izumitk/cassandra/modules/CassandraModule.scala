package org.bitbucket.pshirshov.izumitk.cassandra.modules

import com.datastax.driver.core.policies.TokenAwarePolicy
import com.datastax.driver.core.{Cluster, Session}
import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import org.bitbucket.pshirshov.izumitk.cassandra.PSCache

/**
  */
class CassandraModule() extends CassandraModuleBase {

  override def configure(): Unit = {
    super.configure()
  }

  @Provides
  @Singleton
  override def createCluster
  (
    policy: TokenAwarePolicy
    , @Named("cassandra.endpoints") endpoints: List[String]
  ): Cluster = super.createCluster(policy, endpoints)

  @Provides
  @Singleton
  override def createPreparedStatementsCache
  (
    session: Session
    , @Named("@cassandra.defaults.cache-spec") cacheSpec: String
  ): PSCache = super.createPreparedStatementsCache(session, cacheSpec)

  @Provides
  @Singleton
  @Named("cassandra.keyspace")
  def keyspace(@Named("@cassandra.defaults.keyspace") defaultKeyspace: String): String = {
    defaultKeyspace
  }

  @Provides
  @Singleton
  override def getSession
  (
    cluster: Cluster
    , @Named("@cassandra.defaults.replication") defaultReplication: String
    , @Named("cassandra.keyspace") defaultKeyspace: String
  ): Session = super.getSession(cluster, defaultReplication, defaultKeyspace)
}
