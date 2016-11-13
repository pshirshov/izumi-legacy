package org.bitbucket.pshirshov.izumitk.cassandra.modules

import com.datastax.driver.core.policies.TokenAwarePolicy
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{Cluster, Session}
import com.google.inject.name.{Named, Names}
import com.google.inject.{Provides, Singleton}
import org.bitbucket.pshirshov.izumitk.cassandra.PSCache
import org.bitbucket.pshirshov.izumitk.test.{ExposedTestScope, WithReusableResources}
import net.codingwell.scalaguice.ScalaMultibinder
import org.apache.commons.lang3.RandomStringUtils


object CassandraTestModule {
  final val defaultKeyspace: String = s"tst_fp_${RandomStringUtils.randomAlphabetic(8).toLowerCase}"
}

@ExposedTestScope
final class CassandraTestModule() extends CassandraModuleBase with WithReusableResources {
  override def configure(): Unit = {
    super.configure()

    val namedMulti = ScalaMultibinder.newSetBinder[Class[_]](binder, Names.named("notCloseOnShutdown"))
    namedMulti.addBinding.toInstance(classOf[Cluster])
    namedMulti.addBinding.toInstance(classOf[Session])
  }

  @Provides
  @Singleton
  @Named("cassandra.keyspace")
  def keyspace() = {
    CassandraTestModule.defaultKeyspace
  }

  @Provides
  @Singleton
  override def createCluster
  (
    policy: TokenAwarePolicy
    , @Named("cassandra.endpoints") endpoints: List[String]
  ): Cluster = {
    getResource("CASSANDRA-CLUSTER", () => super.createCluster(policy, endpoints))
  }

  @Provides
  @Singleton
  override def createPreparedStatementsCache
  (
    session: Session
    , @Named("@cassandra.defaults.cache-spec") cacheSpec: String
  ): PSCache = {
    getResource("CASSANDRA-PS-CACHE", () => super.createPreparedStatementsCache(session, cacheSpec))
  }

  @Provides
  @Singleton
  override def getSession
  (
    cluster: Cluster
    , @Named("@cassandra.defaults.replication") defaultReplication: String
    , @Named("cassandra.keyspace") defaultKeyspace: String
  ): Session = {
    getResource("CASSANDRA-SESSION"
      , () => super.getSession(cluster, defaultReplication, defaultKeyspace)
      , {
        session: Session =>
          session.execute(s"DROP KEYSPACE ${QueryBuilder.quote(defaultKeyspace)}")
          logger.info(s"Keyspace `$defaultKeyspace` dropped")
          cluster.close()
      }
    )
  }
}