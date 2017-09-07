package org.bitbucket.pshirshov.izumitk.http

import java.util.UUID

import akka.http.scaladsl.model.{HttpRequest, Uri}
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonSubTypes, JsonTypeInfo, JsonTypeName}
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.{Module, Singleton}
import com.google.inject.name.Names
import com.google.inject.util.Modules
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.bitbucket.pshirshov.izumitk.TestConfigExtensions._
import org.bitbucket.pshirshov.izumitk.app.modules.ConfigExposingModule
import org.bitbucket.pshirshov.izumitk.hal.HalResource
import org.bitbucket.pshirshov.izumitk.http.HalTestPolymorphics.SimpleTextPayload
import org.bitbucket.pshirshov.izumitk.http.hal.decoder.UnreliableHalDecoder
import org.bitbucket.pshirshov.izumitk.http.hal.model.{HalContext, HalEntityContext}
import org.bitbucket.pshirshov.izumitk.http.hal.modules.HalModule
import org.bitbucket.pshirshov.izumitk.http.hal.serializer.{HalHook, HalSerializerImpl}
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import org.bitbucket.pshirshov.izumitk.json.modules.JacksonModule
import org.bitbucket.pshirshov.izumitk.test.InjectorTestBase
import org.bitbucket.pshirshov.izumitk.{HealthStatus, TestConfig}

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
  property = "kind")
@JsonSubTypes(Array(
  new Type(value = classOf[HalTestPolymorphics.SimpleTextPayload])
  , new Type(value = classOf[HalTestPolymorphics.ArbitraryJsonPayload])
))
//@HalProperty
trait HalTestPolymorphic {}

object HalTestPolymorphics {

  @JsonTypeName("text-message")
  case class SimpleTextPayload(text: String) extends HalTestPolymorphic

  @JsonTypeName("json-message") // TODO: just for testing purposes!
  case class ArbitraryJsonPayload(value: JsonNode) extends HalTestPolymorphic

}

class EmptyHalHook extends HalHook with StrictLogging {
  override def handleEntity: PartialFunction[HalEntityContext, Unit] = {
    case _: HalEntityContext =>

  }
}

//@HalProperty
case class HalTestComplexProperty(field: Int = 678)

@HalResource(self = "messages/{?id}")
case class HalTestMessage(id: UUID, payload: HalTestPolymorphic)

@HalResource
case class HalTestEntry(message: HalTestMessage
                        , messages: Seq[HalTestMessage]
                        , nulledSeq: Seq[HalTestMessage]
                        , test01: Int = 123
                        , test02: String = "xxx"
                        , complexProperty: HalTestComplexProperty = HalTestComplexProperty()
                        , arrayProperty: Seq[HalTestComplexProperty] = Seq(HalTestComplexProperty(), HalTestComplexProperty())
                        , mapProperty: Map[String, HalTestComplexProperty] = Map("test" -> HalTestComplexProperty())
                        , resourceMapProperty: Map[String, HalTestMessage] = Map("test" -> HalTestMessage(UUID.randomUUID(), SimpleTextPayload("xxx")))
                       ) {

  def getStatus: HealthStatus = {
      HealthStatus.UNKNOWN
  }

  //noinspection AccessorLikeMethodIsEmptyParen
  def getEmptyParenStatus(): HealthStatus = {
    HealthStatus.UNKNOWN
  }

  @JsonIgnore
  def getIgnoredStatus: HealthStatus = {
    HealthStatus.UNKNOWN
  }
}

class HalSerializerImplTest extends InjectorTestBase {
  "HAL Serializer" must {
    "serialize case classes hierarchy" in withInjector {
      injector =>
        val mapper = injector.instance[HalSerializerImpl]
        val message = HalTestMessage(UUID.randomUUID(), HalTestPolymorphics.SimpleTextPayload("xxx"))

        val sample = HalTestEntry(message, Seq(message, message), null)
        val repr = mapper
          .makeRepr(sample, new HalContext {}, HttpRequest(uri = Uri("http://localhost:8080")))
          .toString(RepresentationFactory.HAL_JSON)

        val decoder = injector.instance[UnreliableHalDecoder]
        val decoded = decoder.readHal[HalTestEntry](repr)
        assert(decoded == sample.copy(nulledSeq = Seq.empty))

        val jMapper = injector.instance[JacksonMapper](Names.named("standardMapper"))

        val tree = jMapper.readTree(repr).asInstanceOf[ObjectNode]
        assert(tree.has("status"))
        assert(tree.has("emptyParenStatus"))
        assert(!tree.has("ignoredStatus"))
        assert(!tree.has("class"))
    }
  }

  override protected val mainTestModule: Module = Modules.combine(
    new JacksonModule()
    , new HalModule()
    , new ConfigExposingModule(TestConfig.references("json"))
    , new ScalaModule {
      override def configure(): Unit = {
        val halMappers = ScalaMultibinder.newSetBinder[HalHook](binder)
        halMappers.addBinding.to[EmptyHalHook].in[Singleton]
      }
    }
  )
}
