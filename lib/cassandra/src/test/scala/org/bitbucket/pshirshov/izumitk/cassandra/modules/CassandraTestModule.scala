package org.bitbucket.pshirshov.izumitk.cassandra.modules

import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

import com.datastax.driver.core.policies.TokenAwarePolicy
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{Cluster, Session}
import com.google.inject.name.{Named, Names}
import com.google.inject.{Provides, Singleton}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaMultibinder
import org.apache.commons.lang3.RandomStringUtils
import org.bitbucket.pshirshov.izumitk.cassandra.PSCache
import org.bitbucket.pshirshov.izumitk.cassandra.facade.{CKeyspace, CKeyspaceId}
import org.bitbucket.pshirshov.izumitk.test.{ExposedTestScope, WithReusableResources}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}


object CassandraTestModule {
  final val uid: String = RandomStringUtils.randomAlphanumeric(4).toLowerCase
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
  @Named("cassandra.keyspaces")
  def keyspace(@Named("@cassandra.defaults.keyspaces.*") defaultKeyspaces: Config): Map[CKeyspaceId, CKeyspace] = {
    defaultKeyspaces.root().unwrapped().asScala.toMap.asInstanceOf[Map[String, String]].map {
      case (alias, ks) =>
        val startTime = ManagementFactory.getRuntimeMXBean.getStartTime
        (CKeyspaceId(alias), CKeyspace(s"tst_${ks}_${startTime}_${CassandraTestModule.uid}"))
    }
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
  protected override def getSession(
                                     cluster: Cluster
                                     , @Named("@cassandra.defaults.replication") defaultReplication: String
                                     , @Named("cassandra.keyspaces") keyspaceAliases: Map[CKeyspaceId, CKeyspace]
                                   ): Session = {
    getResource(s"CASSANDRA-SESSION"
      , () => super.getSession(cluster, defaultReplication, keyspaceAliases)
      , {
        session: Session =>
          keyspaceAliases.foreach {
            case (alias, keyspace) =>
              val future = session.executeAsync(s"DROP KEYSPACE ${QueryBuilder.quote(keyspace.name)}")
              Try(future.get(2, TimeUnit.SECONDS)) match {
                case Success(_) =>
                  logger.info(s"Keyspace `${keyspace.name}` dropped")
                case Failure(f) =>
                  logger.info(s"Keyspace `${keyspace.name}` was not dropped until timeout, but probably it would be dropped later: $f")
              }
          }
          cluster.close()
      }
    )
  }
}
