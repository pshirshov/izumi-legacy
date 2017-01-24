package org.bitbucket.pshirshov.izumitk.http

import java.util.UUID

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonSubTypes, JsonTypeInfo, JsonTypeName}
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.Module
import com.google.inject.name.Names
import com.google.inject.util.Modules
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import org.bitbucket.pshirshov.izumitk.TestConfigExtensions._
import org.bitbucket.pshirshov.izumitk.app.modules.ConfigExposingModule
import org.bitbucket.pshirshov.izumitk.hal.HalResource
import org.bitbucket.pshirshov.izumitk.http.HalTestPolymorphics.SimpleTextPayload
import org.bitbucket.pshirshov.izumitk.http.hal.modules.HalModule
import org.bitbucket.pshirshov.izumitk.http.hal.{HalSerializerImpl, UnreliableHalDecoder}
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

//@HalProperty
case class HalTestComplexProperty(field: Int = 678)

@HalResource(self = "messages/{?id}")
case class HalTestMessage(id: UUID, payload: HalTestPolymorphic)

@HalResource
case class HalTestEntry(message: HalTestMessage
                        , messages: Seq[HalTestMessage]
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
        val sample = HalTestEntry(message, Seq(message, message))
        val repr = mapper.makeRepr("http://localhost:8080", sample, {
          ctx =>
            ctx.repr.withLink("xxx", "yyy")
        })
          .toString(RepresentationFactory.HAL_JSON)

        val decoder = injector.instance[UnreliableHalDecoder]
        val decoded = decoder.readHal[HalTestEntry](repr)
        assert(decoded == sample)

        val jMapper = injector.instance[JacksonMapper](Names.named("standardMapper"))

        val tree = jMapper.readTree(repr).asInstanceOf[ObjectNode]
        assert(tree.has("status"))
        assert(tree.has("emptyParenStatus"))
        assert(!tree.has("ignoredStatus"))
    }
  }

  override protected val modules: Module = Modules.combine(
    new JacksonModule()
    , new HalModule()
    , new ConfigExposingModule(TestConfig.references("json"))
  )
}
