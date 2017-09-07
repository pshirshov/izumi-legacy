package org.bitbucket.pshirshov.izumitk.json.modules

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonTypeInfo, PropertyAccessor}
import com.fasterxml.jackson.core.{JsonParser, JsonToken}
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.common.reflect.ClassPath
import com.google.inject.name.{Named, Names}
import com.google.inject.{Provides, Singleton}
import com.typesafe.scalalogging.StrictLogging
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper

import scala.collection.immutable
import scala.reflect.ClassTag

/**
  */
final class JacksonModule() extends ScalaModule {
  override def configure(): Unit = {
    ScalaMultibinder.newSetBinder[Module](binder, Names.named("json.domain.modules"))
  }

  @Provides
  @Singleton
  @Named("standardMapper")
  def standardMapper(@Named("withModulesMapper") bm: JacksonMapper): JacksonMapper = {
    bm.setSerializationInclusion(Include.NON_NULL)
    bm
  }

  @Provides
  @Singleton
  @Named("typingMapper")
  def typingMapper(@Named("withModulesMapper") bm: JacksonMapper): JacksonMapper = {
    bm.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
    bm.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    bm.setVisibility( PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY )
    bm
  }

  @Provides
  @Singleton
  @Named("permissiveMapper")
  def permissiveMapper(@Named("withModulesMapper") bm: JacksonMapper): JacksonMapper = {
    bm.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    bm.setSerializationInclusion(Include.NON_NULL)
    bm
  }

  @Provides
  @Named("basicMapper")
  protected def createBasicMapper(@Named("@json.compact") compact: Boolean): JacksonMapper = {
    val m = new JacksonMapper
    m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    m.configure(SerializationFeature.INDENT_OUTPUT, !compact)
    m
  }

  @Provides
  @Named("withModulesMapper")
  def withModules(
                    @Named("basicMapper") bm: JacksonMapper
                    , @Named("json.domain.modules") domainModules: immutable.Set[Module]
                  ): JacksonMapper = {
    val jacksonModules = Seq(DefaultScalaModule, new JavaTimeModule)
    bm.registerModules(jacksonModules: _*)
    bm.registerModules(domainModules.toSeq: _*)
    bm
  }
}

abstract class AbstractDomainExtensionsModule
  extends ScalaModule
  with StrictLogging {

  protected implicit class SimpleModuleExtensions(module: SimpleModule) {
    def addStringValConstructorDeserializer[T: ClassTag](): SimpleModule = {
      val runtimeClass: Class[T] = scala.reflect.classTag[T].runtimeClass.asInstanceOf[Class[T]]

      module.addDeserializer(runtimeClass, (p: JsonParser, ctxt: DeserializationContext) => {
        val currentToken = p.getCurrentToken

        if (currentToken.equals(JsonToken.VALUE_STRING)) {
          val text = p.getText.trim()
          runtimeClass.getConstructors.find(_.getParameterTypes.toSeq == Seq(classOf[String])).get.newInstance(text).asInstanceOf[T]
        } else {
          ctxt.handleUnexpectedToken(runtimeClass, p).asInstanceOf[T]
          //throw ctxt.mappingException(runtimeClass)
        }
      })
    }

    def addStringValParsingDeserializer[T <: AnyRef : Manifest](parser: String => T): SimpleModule = {
      val clazz = manifest[T].runtimeClass.asInstanceOf[Class[T]]

      module.addKeyDeserializer(clazz, (key: String, ctxt: DeserializationContext) => {
        parser(key)
      })

      module.addDeserializer(clazz, (p: JsonParser, ctxt: DeserializationContext) => {
        val currentToken = p.getCurrentToken

        if (currentToken.equals(JsonToken.VALUE_STRING)) {
          val text = p.getText.trim()
          parser(text)
        } else {
          ctxt.handleUnexpectedToken(clazz, p).asInstanceOf[T]
        }
      })
    }

    def addPolymorphicClass(polymorphicClass: Class[_], pkg: Package): SimpleModule = {
      addPolymorphicClass(polymorphicClass, pkg.getName)
    }

    def addPolymorphicClass(polymorphicClass: Class[_], pkgName: String): SimpleModule = {
      import scala.collection.JavaConverters._
      val classpath = ClassPath.from(polymorphicClass.getClassLoader)
      val implementations = classpath
        .getAllClasses
        .asScala
        .filter(_.getPackageName.startsWith(pkgName))
        .map(_.load())
        .filter(polymorphicClass.isAssignableFrom)
        .toSeq

      logger.debug(s"Registering subtypes for $polymorphicClass: $implementations")

      module.registerSubtypes(implementations: _*)
    }

  }


}
