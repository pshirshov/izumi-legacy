package org.bitbucket.pshirshov.izumitk.plugins

import com.google.inject.Singleton
import org.bitbucket.pshirshov.izumitk.Depends
import org.bitbucket.pshirshov.izumitk.cdi.GuicePlugin
import org.bitbucket.pshirshov.izumitk.failures.services.{CassandraFailureRespository, FailureRepository}
import org.bitbucket.pshirshov.izumitk.failures.services.targets.FailureRepositoryTarget

/**
  */
@Depends(Array(classOf[CassandraPlugin]))
class CassandraFailureRepositoryPlugin extends GuicePlugin with FailureRepositoryTarget {
  override def configure(): Unit = {
    bind[FailureRepository].to[CassandraFailureRespository].in[Singleton]
  }
}
