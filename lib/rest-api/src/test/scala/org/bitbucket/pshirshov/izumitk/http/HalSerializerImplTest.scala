package org.bitbucket.pshirshov.izumitk.http

import java.util.UUID

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo, JsonTypeName}
import com.fasterxml.jackson.databind.JsonNode
import com.google.inject.Module
import com.google.inject.util.Modules
import org.bitbucket.pshirshov.izumitk.hal.HalResource
import org.bitbucket.pshirshov.izumitk.TestConfig
import org.bitbucket.pshirshov.izumitk.app.modules.ConfigExposingModule
import org.bitbucket.pshirshov.izumitk.http.hal.{HalSerializerImpl, UnreliableHalDecoder}
import org.bitbucket.pshirshov.izumitk.http.modules.HalModule
import org.bitbucket.pshirshov.izumitk.json.modules.JacksonModule
import org.bitbucket.pshirshov.izumitk.test.InjectorTestBase
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import org.bitbucket.pshirshov.izumitk.http.TestPolymorphics.SimpleTextPayload


@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
  property = "kind")
@JsonSubTypes(Array(
  new Type(value = classOf[TestPolymorphics.SimpleTextPayload])
  , new Type(value = classOf[TestPolymorphics.ArbitraryJsonPayload])
))
//@HalProperty
trait TestPolymorphic {}

object TestPolymorphics {
  @JsonTypeName("text-message")
  case class SimpleTextPayload(text: String) extends TestPolymorphic

  @JsonTypeName("json-message") // TODO: just for testing purposes!
  case class ArbitraryJsonPayload(value: JsonNode) extends TestPolymorphic

}

//@HalProperty
case class ComplexProperty(field: Int = 678)

@HalResource(self = "messages/{?id}")
case class HistoricMessage(id: UUID, payload: TestPolymorphic)

@HalResource
case class HistoryEntry(message: HistoricMessage
                        , messages: Seq[HistoricMessage]
                        , test01: Int = 123
                        , test02: String = "xxx"
                        , complexProperty: ComplexProperty = ComplexProperty()
                        , arrayProperty: Seq[ComplexProperty] = Seq(ComplexProperty(), ComplexProperty())
                        , mapProperty: Map[String, ComplexProperty] = Map("test" -> ComplexProperty())
                        , resourceMapProperty: Map[String, HistoricMessage] = Map("test" -> HistoricMessage(UUID.randomUUID(), SimpleTextPayload("xxx")))
                       )

class HalSerializerImplTest extends InjectorTestBase {
  "HAL Serializer" must {
    "serialize case classes hierarchy" in withInjector {
      injector =>
        val mapper = injector.instance[HalSerializerImpl]
        val message = HistoricMessage(UUID.randomUUID(), TestPolymorphics.SimpleTextPayload("xxx"))
        val sample = HistoryEntry(message, Seq(message, message))
        val repr = mapper.makeRepr("http://localhost:8080", sample, {
          ctx =>
            ctx.repr.withLink("xxx", "yyy")
        })
          .toString(RepresentationFactory.HAL_JSON)

        //println(repr) // TODO: XXX: real test

        val decoder =  injector.instance[UnreliableHalDecoder]
        assert(decoder.readHal[HistoryEntry](repr) == sample)
    }
  }

  override protected val modules: Module = Modules.combine(
    new JacksonModule()
    , new HalModule()
    , new ConfigExposingModule(TestConfig.references("json"))
  )
}
