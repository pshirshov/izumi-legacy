package org.bitbucket.pshirshov.izumitk.plugins

import com.google.inject
import com.google.inject.Inject
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
import org.bitbucket.pshirshov.izumitk.cdi.Plugin
import org.bitbucket.pshirshov.izumitk.{Depends, ExtensionPoint, RequiredConfig, TargetPoint}

@ExtensionPoint
trait TestExtender1 {

}

@ExtensionPoint
trait TestExtender2 {

}

trait TestService1 {
  def doSomething(): Int = 42
}
trait TestService2 {
  def doAnything(): Int
}

class TestService1Impl extends TestService1 {}

class TestService2Impl @Inject() (service1: TestService1) extends TestService2 {
  override def doAnything(): Int = service1.doSomething()
}

class TestConfiglessPlugin extends Plugin with TestExtender1 with TestExtender2 {

}

class TestPlugin @RequiredConfig("test.config") (val config: Config) extends Plugin with TestExtender1 with TestExtender2 {
  override def createPluginModules: Seq[ScalaModule] = Seq(new ScalaModule {
    override def configure(): Unit = {
      bind[TestService1].to[TestService1Impl].in[inject.Singleton]
      bind[TestService2].to[TestService2Impl].in[inject.Singleton]
    }
  })
}

trait IDep {}

@Depends(Array(classOf[IDep]))
class TestDepPlugin1 extends Plugin {
}

class TestDepPlugin2 extends Plugin with IDep {
}

class TestDepPlugin3 extends Plugin {
}

class TestDepPlugin4 extends Plugin {
}


class TestDepPluginDisabled extends Plugin {
}

class TestDepPluginDisabled2 extends Plugin {
}

class DefaultConfigurablePlugin @RequiredConfig() (val config: Config) extends Plugin {
  override def createPluginModules: Seq[ScalaModule] = Seq()
}

class DefaultConfigurablePlugin1 @RequiredConfig() (val config: Config) extends Plugin {
  override def createPluginModules: Seq[ScalaModule] = Seq()
}

@TargetPoint
trait TestTarget {

}

class TestTargetPlugin1 extends Plugin with TestTarget {
}

class TestTargetPlugin2 extends Plugin with TestTarget {
}

@TargetPoint
trait TestTarget1 {

}

class TestTargetPlugin3 extends Plugin with TestTarget1 {
}

class TestTargetPlugin4 extends Plugin with TestTarget1 {
}

class TestPluginRegexDisabledXXX extends Plugin {
}
