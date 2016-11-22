package org.bitbucket.pshirshov.izumitk.plugins

import com.google.inject.Singleton
import org.bitbucket.pshirshov.izumitk.Depends
import org.bitbucket.pshirshov.izumitk.cdi.GuicePlugin
import org.bitbucket.pshirshov.izumitk.failures.services.{CassandraFailureRespository, FailureRepositoryTarget}
import org.bitbucket.pshirshov.izumitk.services.FailureRepository

/**
  */
@Depends(Array(classOf[CassandraPlugin]))
class CassandraFailureRepositoryPlugin extends GuicePlugin with FailureRepositoryTarget {
  override def configure(): Unit = {
    bind[FailureRepository].to[CassandraFailureRespository].in[Singleton]
  }
}
