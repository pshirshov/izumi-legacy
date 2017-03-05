package org.bitbucket.pshirshov.izumitk.test

import com.google.inject.Module
import org.bitbucket.pshirshov.izumitk.TestConfig.TestConfigSection
import org.bitbucket.pshirshov.izumitk.plugins.ClusterNodeIdSimplePlugin

/**
  */
@ExposedTestScope
trait GenericClusterTest extends EnvironmentalTest {
  abstract override protected def environment: Seq[Module] = super.environment ++ plugins(new ClusterNodeIdSimplePlugin())

  abstract override protected def requiredReferences: Seq[TestConfigSection] = super.requiredReferences.and(
    "clustering"
  )
}
