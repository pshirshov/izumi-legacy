package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import java.util
import java.util.UUID

import akka.http.scaladsl.model.HttpRequest
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.theoryinpractise.halbuilder.api.{Representation, RepresentationFactory}
import org.apache.commons.lang3.StringUtils
import org.bitbucket.pshirshov.izumitk.hal.HalResource
import org.bitbucket.pshirshov.izumitk.http.hal.model.{HalContext, HalEntityContext, HalRequestContext}
import org.bitbucket.pshirshov.izumitk.http.hal.serializer.links.LinkExtractor
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._


@Singleton
class HalSerializerImpl @Inject()
(
  @Named("standardMapper") mapper: JacksonMapper
  , factory: RepresentationFactory
  , linkExtractor: LinkExtractor
  , hooks: HalHooks
) extends HalSerializer {
  override def makeRepr(
                         dto: Any
                         , hc: HalContext
                         , rc: HttpRequest
                       ): Representation = {
    val baseUri = linkExtractor.extract(Option(rc))
    val requestContext = HalRequestContext(baseUri, rc)

    val repr = factory.newRepresentation()
    val ec = hooks.reduce(HalEntityContext(hc, requestContext, dto, repr))


    dto match {
      case tree: ObjectNode =>
        serializeTree(tree, repr)
      case hr =>
        serializeDto(ec)
    }

    hooks.handleEntity(ec)
    repr
  }

  private def serializeTree(tree: ObjectNode, repr: Representation): Unit = {
    tree.fields().asScala.foreach {
      e =>
        repr.withProperty(e.getKey, e.getValue)
    }
  }

  private def serializeDto(ec: HalEntityContext): Unit = {
    Option(ec.dto.getClass.getAnnotation(classOf[HalResource])).foreach {
      rann =>
        Option(rann.self()).filter(_.nonEmpty).foreach {
          self =>
            ec.repr.withLink("self", s"${ec.rc.baseUri}/$self")
        }
      // TODO: anything else?..
    }

    val rm = scala.reflect.runtime.currentMirror
    val instanceMirror = rm.reflect(ec.dto)

    rm.classSymbol(ec.dto.getClass).toType.members.collect {
      case m: MethodSymbol if isPublicGetter(m) => m
      case m: MethodSymbol if isGetterLike(m) => m
    }.foreach {
      acc =>
        val rawName = acc.name.decodedName.toString
        val name = if (rawName.startsWith("get")) {
          StringUtils.uncapitalize(rawName.substring(3))
        } else {
          rawName
        }

        val value = instanceMirror.reflectMethod(acc).apply()
        value match {
          case null =>
            ec.repr.withProperty(name, value)
          case _: Number =>
            ec.repr.withProperty(name, value)
          case _: String =>
            ec.repr.withProperty(name, value)
          case _: UUID =>
            ec.repr.withProperty(name, value.toString)
          case _: Boolean =>
            ec.repr.withProperty(name, value)

          // TODO: improve maps support: we need to handle embedded hal resources as well
          case v: util.Map[_, _] =>
            fillMap(ec, name, v.asScala.toMap)

          case v: Map[_, _] =>
            fillMap(ec, name, v)

          case v: util.Collection[_] =>
            fillSequence(ec, name, v.asScala.toSeq)

          case v: Traversable[_] =>
            fillSequence(ec, name, v.toSeq)

          case v: AnyRef if isHalResource(v) =>
            ec.repr.withRepresentation(name, makeRepr(v, ec.hc, ec.rc.rc))

          case v: AnyRef => //if isHalProperty(v) =>
            ec.repr.withProperty(name, mapper.valueToTree(v))

          case _ =>
            throw new UnsupportedOperationException(s"We don't know how to serialize `$value` in `$ec`")
        }
    }
  }

  private def fillMap(ec: HalEntityContext, name: String, v: Map[_, _]): Any = {
    v.partition(pair => isHalResource(pair._2)) match {
      case (resources: Map[_, _], values: Map[_, _]) =>
        val node = mapper.getNodeFactory.objectNode()

        resources.foreach {
          resource =>
            val rrepr = makeRepr(resource._2, ec.hc, ec.rc.rc)
            val asJson = rrepr.toString(RepresentationFactory.HAL_JSON)
            node.set(getLabel(resource._1), mapper.readValue[ObjectNode](asJson))
        }

        values.foreach {
          value =>
            node.set(getLabel(value._1), mapper.valueToTree(value._2))
        }

        ec.repr.withProperty(name, mapper.valueToTree(node))
    }
  }


  private def getLabel(value: Any): String =
    value match {
      case s: String => s
      case _ => mapper.convertValue[String](value)
    }

  private def fillSequence(ec: HalEntityContext, name: String, v: Traversable[_]): Unit = {
    serializeSequence(ec, v) match {
      case (resources, properties) =>
        if (properties.nonEmpty) {
          val arr = mapper.getNodeFactory.arrayNode()
          properties.foreach(v => arr.add(mapper.valueToTree[JsonNode](v)))
          ec.repr.withProperty(name, arr)
        }

        resources.foreach(r => ec.repr.withRepresentation(name, r))
    }
  }

  private def serializeSequence(ec: HalEntityContext, sequence: Traversable[_]): (Seq[Representation], Seq[JsonNode]) = {
    sequence.partition(isHalResource) match {
      case (resources, properties) =>
        (
          resources.map(r => makeRepr(r, ec.hc, ec.rc.rc)).toSeq
          , properties.map(p => mapper.valueToTree[JsonNode](p)).toSeq
        )
    }
  }

  private def isHalResource(value: Any): Boolean = {
    value.getClass.isAnnotationPresent(classOf[HalResource])
  }

  private def isGetterLike(m: _root_.scala.reflect.runtime.universe.MethodSymbol) = {
    m.paramLists.forall(_.isEmpty) &&
      m.isPublic &&
      m.name.decodedName.toString.startsWith("get") &&
      !(m.returnType.erasure =:= typeTag[Class[_]].tpe.erasure) &&
      !m.annotations.exists(_.tree.tpe == typeTag[JsonIgnore].tpe)
  }

  private def isPublicGetter(m: _root_.scala.reflect.runtime.universe.MethodSymbol) = {
    m.isGetter && m.isPublic
  }

  //  private def isHalProperty(value: AnyRef): Boolean = {
  //    (Seq(value.getClass) ++ value.getClass.getInterfaces.toSeq)
  //      .flatMap(_.getAnnotations)
  //      .exists(_.annotationType() == classOf[HalProperty])
  //  }
}
