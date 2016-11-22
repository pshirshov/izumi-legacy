package org.bitbucket.pshirshov.izumitk.plugins

import com.google.inject.Singleton
import org.bitbucket.pshirshov.izumitk.cdi.GuicePlugin
import org.bitbucket.pshirshov.izumitk.failures.NullFailureRepository
import org.bitbucket.pshirshov.izumitk.failures.services.FailureRepository
import org.bitbucket.pshirshov.izumitk.failures.services.targets.FailureRepositoryTarget

/**
  */
class NullFailureRepositoryPlugin extends GuicePlugin with FailureRepositoryTarget {
  override def configure(): Unit = {
    bind[FailureRepository].to[NullFailureRepository].in[Singleton]
  }
}
