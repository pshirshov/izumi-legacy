package org.bitbucket.pshirshov.izumitk.cassandra.modules

import com.datastax.driver.core.policies.TokenAwarePolicy
import com.datastax.driver.core.{Cluster, Session}
import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.Config
import org.bitbucket.pshirshov.izumitk.cassandra.PSCache
import org.bitbucket.pshirshov.izumitk.cassandra.facade.{CKeyspace, CKeyspaceId}

import scala.collection.JavaConverters._

/**
  */
class CassandraModule()
  extends CassandraModuleBase {

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
  @Named("cassandra.keyspaces")
  def keyspace(@Named("@cassandra.defaults.keyspaces.*") defaultKeyspaces: Config): Map[CKeyspaceId, CKeyspace] = {
    defaultKeyspaces.root().unwrapped().asScala.toMap.asInstanceOf[Map[String, String]].map(kv => (CKeyspaceId(kv._1), CKeyspace(kv._2)))
  }

  @Provides
  @Singleton
  override def getSession
  (
    cluster: Cluster
    , @Named("@cassandra.defaults.replication") defaultReplication: String
    , @Named("cassandra.keyspaces") keyspaceAliases: Map[CKeyspaceId, CKeyspace]
  ): Session = super.getSession(cluster, defaultReplication, keyspaceAliases)
}
