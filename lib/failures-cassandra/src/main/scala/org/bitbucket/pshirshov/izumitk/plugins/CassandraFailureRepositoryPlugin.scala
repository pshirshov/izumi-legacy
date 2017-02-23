package org.bitbucket.pshirshov.izumitk.plugins

import com.google.inject.name.Named
import com.google.inject.{Provides, Singleton}
import org.bitbucket.pshirshov.izumitk.Depends
import org.bitbucket.pshirshov.izumitk.cdi.GuicePlugin
import org.bitbucket.pshirshov.izumitk.failures.services.{CassandraFailureRepository, FailureRepository}
import org.bitbucket.pshirshov.izumitk.failures.targets.FailureRepositoryTarget
import org.bitbucket.pshirshov.izumitk.util.TimeUtils

import scala.concurrent.duration.FiniteDuration

/**
  */
@Depends(Array(classOf[CassandraPlugin]))
class CassandraFailureRepositoryPlugin extends GuicePlugin with FailureRepositoryTarget {
  override def configure(): Unit = {
    bind[FailureRepository].to[CassandraFailureRepository].in[Singleton]
  }

  @Provides
  @Singleton
  @Named("failures.records-ttl")
  def ttl(@Named("@failures.records-ttl") ttl: String): FiniteDuration = TimeUtils.parseFinite(ttl)
}
