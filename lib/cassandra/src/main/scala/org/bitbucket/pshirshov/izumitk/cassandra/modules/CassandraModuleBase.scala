package org.bitbucket.pshirshov.izumitk.cassandra.modules

import com.datastax.driver.core.exceptions.AlreadyExistsException
import com.datastax.driver.core.policies.{DCAwareRoundRobinPolicy, RoundRobinPolicy, TokenAwarePolicy}
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{Cluster, PreparedStatement, ProtocolOptions, Session}
import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.inject.{Provides, Scopes, Singleton}
import com.google.inject.name.Named
import com.typesafe.config.{Config, ConfigList}
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.bitbucket.pshirshov.izumitk.HealthChecker
import org.bitbucket.pshirshov.izumitk.cassandra._
import org.bitbucket.pshirshov.izumitk.cassandra.facade.{CKeyspace, CKeyspaceId}
import org.bitbucket.pshirshov.izumitk.util.network.NetworkUtils

import scala.collection.JavaConverters._


/**
  */
abstract class CassandraModuleBase() extends ScalaModule with StrictLogging {
  override def configure(): Unit = {
    val healthCheckers = ScalaMultibinder.newSetBinder[HealthChecker](binder)
    healthCheckers.addBinding.to(classOf[CassandraHealthChecker]).in(Scopes.SINGLETON)
  }

  @Provides
  @Singleton
  @Named("cassandra.endpoints")
  final def endpoints (@Named("@cassandra.nodes[]") endpoints: ConfigList): List[String] = {
    endpoints.asScala.map(_.unwrapped().toString).toList
  }

  @Provides
  @Singleton
  final def getPolicy(@Named("@cassandra.policy") policyName: String, @Named("@cassandra.policies.*") policiesConfig: Config): TokenAwarePolicy = {
    // TODO: reflection
    val policy = policyName match {
      case "DCAwareRoundRobinPolicy" =>
        val policyConfig = policiesConfig.getConfig(policyName)
        val localDC = policyConfig.getString("local-dc")
        val allowRemoteDCsForLocalConsistencyLevel = policyConfig.getBoolean("allowRemoteDCsForLocalConsistencyLevel")
        val usedHostsPerRemoteDc = policyConfig.getInt("usedHostsPerRemoteDc")
        val policyBuilder = DCAwareRoundRobinPolicy
          .builder()
          .withLocalDc(localDC)
          .withUsedHostsPerRemoteDc(usedHostsPerRemoteDc)

        if (allowRemoteDCsForLocalConsistencyLevel) {
          policyBuilder.allowRemoteDCsForLocalConsistencyLevel()
        }

        new TokenAwarePolicy(policyBuilder.build())


      case "RoundRobinPolicy" =>
        new TokenAwarePolicy(new RoundRobinPolicy())
    }
    policy
  }

  protected def createCluster(policy: TokenAwarePolicy, endpoints: List[String]): Cluster = {
    import scala.collection.JavaConverters._

    val builder = Cluster.builder()

    endpoints.foreach {
      e =>
        val addr = NetworkUtils.getAddress(e, ProtocolOptions.DEFAULT_PORT)
        builder.addContactPoint(addr.getHostName).withPort(addr.getPort)
    }

    logger.info(s"Using policy `$policy`...")

    builder.withLoadBalancingPolicy(policy)

    val newCluster = builder.build()

    val metadata = newCluster.getMetadata
    logger.info("Using to cassandra cluster: {}", metadata.getClusterName)
    metadata.getAllHosts.asScala.foreach {
      host =>
        logger.info("Cassandra node - Datacenter: {}; Host: {}; Rack: {}", host.getDatacenter, host.getAddress,
          host.getRack)
    }

    newCluster
  }

  protected def getSession(cluster: Cluster, defaultReplication: String, keyspaceAliases: Map[CKeyspaceId, CKeyspace]): Session = {
    val session = cluster.newSession()

    keyspaceAliases.foreach {
      case (alias, keyspace) =>
        try {
          session.execute(s"CREATE KEYSPACE ${QueryBuilder.quote(keyspace.name)} WITH REPLICATION = $defaultReplication")
          logger.info(s"Keyspace `${keyspace.name}` aliased as `${alias.id}` with replication $defaultReplication created")
        } catch {
          case _: AlreadyExistsException =>
            logger.debug(s"Keyspace already exists: `${keyspace.name}` aliased as `${alias.id}`")
        }
    }

    //session.execute(s"USE ${QueryBuilder.quote(keyspace)}")
    session
  }

  protected def createPreparedStatementsCache(
                                               session: Session
                                               , cacheSpec: String
                                             ): PSCache = {
    CacheBuilder.from(cacheSpec)
      .build(new CacheLoader[String, PreparedStatement]() {
        override def load(key: String): PreparedStatement = {
          session.prepare(key)
        }
      })
  }
}
