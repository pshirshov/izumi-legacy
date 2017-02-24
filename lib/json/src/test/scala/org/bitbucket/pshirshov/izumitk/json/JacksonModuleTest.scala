package org.bitbucket.pshirshov.izumitk.json


import java.time.{Duration, ZonedDateTime}

import com.google.inject.Module
import com.google.inject.name.Names
import com.google.inject.util.Modules
import org.bitbucket.pshirshov.izumitk.TestConfig
import org.bitbucket.pshirshov.izumitk.TestConfigExtensions._
import org.bitbucket.pshirshov.izumitk.app.modules.ConfigExposingModule
import org.bitbucket.pshirshov.izumitk.json.modules.JacksonModule
import org.bitbucket.pshirshov.izumitk.test.InjectorTestBase
import org.bitbucket.pshirshov.izumitk.util.types.TimeUtils


/**
  *
  */
class JacksonModuleTest extends InjectorTestBase {
  "Jackson mapper" must {
    "serialize and restore timestamps correctly" in withInjector {
      injector =>
        val mapper = injector.instance[JacksonMapper](Names.named("standardMapper"))

        val timestamp = TimeUtils.utcNow
        val serializedTs = mapper.writeValueAsString(timestamp)
        val restored = mapper.readValue[ZonedDateTime](serializedTs)
        assert(Duration.between(timestamp, restored).isZero)
        assert(restored == timestamp)
    }
  }

  override protected val modules: Module = Modules.combine(
    new JacksonModule()
    , new ConfigExposingModule(TestConfig.references("json"))
  )
}
