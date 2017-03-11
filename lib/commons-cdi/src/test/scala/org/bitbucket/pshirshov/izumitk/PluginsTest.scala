package org.bitbucket.pshirshov.izumitk

import com.google.inject.Guice
import com.google.inject.name.Names
import com.typesafe.config.ConfigFactory
import net.codingwell.scalaguice.InjectorExtensions._
import org.bitbucket.pshirshov.izumitk.cdi.{BunchOfModules, Plugin}
import org.bitbucket.pshirshov.izumitk.config.{LoadedConfig, LoadedResource}
import org.bitbucket.pshirshov.izumitk.modularity.GuicePluginsSupport
import org.bitbucket.pshirshov.izumitk.test.IzumiTestBase
import org.bitbucket.pshirshov.izumitk.testplugins._

import scala.language.postfixOps

class TestLoader extends GuicePluginsSupport {
  private val testConfig = ConfigFactory.load("plugins-test.conf")

  override protected val config: LoadedConfig = LoadedResource(testConfig, testConfig, testConfig)

  def loadModulesTest(): Seq[BunchOfModules] = loadPluginModules().modules

  override protected def namespace: String = "testplugins"
}

class PluginsTest extends IzumiTestBase {
  "Plugin loader" must {
    "load plugins" in {
      td =>
        val loader = new TestLoader()
        val modules = loader.loadModulesTest()
        assert(modules.size >= 2)
        val injector = Guice.createInjector(modules.flatMap(_.modules) : _*)
        assert(injector.instance[Seq[Plugin]](Names.named("app.plugins")).size == 11)

        assert(injector.instance[Plugin](Names.named(s"app.plugins.${classOf[TestPlugin].getCanonicalName}"))
          .asInstanceOf[TestPlugin].config.getInt("xxx") == 123)
        assert(injector.instance[TestPlugin].config.getInt("xxx") == 123)
        assert(injector.instance[Set[TestExtender1]].size == 2)
        assert(injector.instance[TestService2].doAnything() == 42)
    }
  }

  "plugins" must {
    "support comparison" in {
      td =>
        val p1 = new TestDepPlugin1()
        val p2 = new TestDepPlugin2()
        val p3 = new TestDepPlugin3()
        val p4 = new TestDepPlugin4()

        assert(p3.compareTo(p4) == 0)
        assert(p4.compareTo(p3) == 0)
        assert(p1 > p2)
        assert(p2 < p1)

        assert(Seq(p1, p2).sorted.head == p2)
        assert(Seq(p2, p1).sorted.head == p2)
    }
  }
}
