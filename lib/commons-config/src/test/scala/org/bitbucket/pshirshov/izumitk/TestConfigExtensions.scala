package org.bitbucket.pshirshov.izumitk

import org.bitbucket.pshirshov.izumitk.TestConfig.TestConfigSection
import org.bitbucket.pshirshov.izumitk.test.ExposedTestScope

import scala.language.implicitConversions


@ExposedTestScope
trait TestConfigExtensions {

  protected implicit class TestConfigSeqExtensions(sections: Seq[TestConfigSection]) {
    def and(sectionNames: TestConfigSection*): Seq[TestConfigSection] = {
      sections ++ sectionNames
    }
  }

  protected implicit def toConfigSection(section: (String, String)): TestConfigSection = {
    TestConfigSection(section._1, section._2)
  }

  protected implicit def toConfigSection(resourceName: String): TestConfigSection = TestConfigSection(resourceName, resourceName)
}

@ExposedTestScope
object TestConfigExtensions extends TestConfigExtensions {

  implicit class PublicTestConfigSeqExtensions(sections: Seq[TestConfigSection]) extends TestConfigSeqExtensions(sections)

  override implicit def toConfigSection(section: (String, String)): TestConfigSection = super.toConfigSection(section)

  override implicit def toConfigSection(resourceName: String): TestConfigSection = super.toConfigSection(resourceName)
}
