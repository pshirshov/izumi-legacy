package org.bitbucket.pshirshov.izumitk.http.hal.serializer

import java.math.BigInteger
import java.util
import java.util.UUID

import akka.http.scaladsl.model.HttpRequest
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.theoryinpractise.halbuilder5.{Rels, ResourceRepresentation}
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
  jacksonHalSerializer: JacksonHalSerializer
  , @Named("standardMapper") mapper: JacksonMapper
  , linkExtractor: LinkExtractor
  , hooks: HalHooks
) extends HalSerializer {
  override def makeRepr(
                         dto: Any
                         , hc: HalContext
                         , rc: HttpRequest
                       ): ResourceRepresentation[ObjectNode] = {
    val baseUri = linkExtractor.extract(Option(rc))
    val requestContext = HalRequestContext(baseUri, rc)

    val repr = ResourceRepresentation.create(mapper.createObjectNode())
    val ec = hooks.reduce(HalEntityContext(hc, requestContext, dto, repr))

    val rRR = dto match {
      case tree: ObjectNode =>
        serializeTree(tree, repr)
      case _ =>
        serializeDto(ec)
    }

    hooks.handleEntity(ec.copy(repr = rRR))
  }

  private def serializeTree(tree: ObjectNode, repr: ResourceRepresentation[ObjectNode]): ResourceRepresentation[ObjectNode] = {
    repr.withValue(tree)
  }

  private def serializeDto(ec: HalEntityContext): ResourceRepresentation[ObjectNode] = {
    val ecRepr = (for {
      rann <- Option(ec.dto.getClass.getAnnotation(classOf[HalResource]))
      self <- Option(rann.self()).filter(_.nonEmpty)
    } yield  // TODO: anything else?..
      ec.repr.withLink("self", s"${ec.rc.baseUri}/$self")
    ) getOrElse ec.repr

    val rm = scala.reflect.runtime.currentMirror
    val instanceMirror = rm.reflect(ec.dto)

    rm.classSymbol(ec.dto.getClass).toType.members.collect {
      case m: MethodSymbol if isPublicGetter(m) => m
      case m: MethodSymbol if isGetterLike(m) => m
    }.foldLeft(ecRepr) {
      (repr, sym) =>
        val rawName = sym.name.decodedName.toString
        val name = if (rawName.startsWith("get")) {
          StringUtils.uncapitalize(rawName.substring(3))
        } else {
          rawName
        }

        val value = instanceMirror.reflectMethod(sym).apply()
        value match {
          case null =>
            repr.map(_.putNull(name))
          case v: java.lang.Short =>
            repr.map(_.put(name, v))
          case v: Short =>
            repr.map(_.put(name, v))
          case v: java.lang.Double =>
            repr.map(_.put(name, v))
          case v: Double =>
            repr.map(_.put(name, v))
          case v: BigInteger =>
            repr.map(_.put(name, v))
          case v: BigInt =>
            repr.map(_.put(name, v.bigInteger))
          case v: Integer =>
            repr.map(_.put(name, v))
          case v: Int =>
            repr.map(_.put(name, v))
          case v: java.lang.Boolean =>
            repr.map(_.put(name, v))
          case v: Boolean =>
            repr.map(_.put(name, v))
          case v: java.math.BigDecimal =>
            repr.map(_.put(name, v))
          case v: BigDecimal =>
            repr.map(_.put(name, v.bigDecimal))
          case v: java.lang.Long =>
            repr.map(_.put(name, v))
          case v: Long =>
            repr.map(_.put(name, v))
          case v: java.lang.Float =>
            repr.map(_.put(name, v))
          case v: Float =>
            repr.map(_.put(name, v))
          case v: String =>
            repr.map(_.put(name, v))
          case v: UUID =>
            repr.map(_.put(name, v.toString))
          case v: util.Map[_, _] =>
            fillMap(ec, name, v.asScala.toMap)
          case v: Map[_, _] =>
            fillMap(ec, name, v)
          case v: util.Collection[_] =>
            fillSequence(ec, name, v.asScala)
          case v: Traversable[_] =>
            fillSequence(ec, name, v)
          case v: AnyRef if isHalResource(v) =>
            val reprWithCollection = repr.withRel(Rels.collection(name))
            reprWithCollection.withRepresentation(name, makeRepr(v, ec.hc, ec.rc.rc))
          case v: AnyRef =>
            repr.map(_.set(name, mapper.valueToTree[ObjectNode](v)).asInstanceOf[ObjectNode])
          case _ =>
            throw new UnsupportedOperationException(s"We don't know how to serialize `$value` in `$ec`")
        }
    }
  }

  private def fillMap(ec: HalEntityContext, name: String, v: Map[_, _]): ResourceRepresentation[ObjectNode] = {
    v.partition(pair => isHalResource(pair._2)) match {
      case (resources: Map[_, _], values: Map[_, _]) =>
        val mutnode = ec.repr.get().`with`(name)

        values.foreach {
          value =>
            mutnode.set(getLabel(value._1), mapper.valueToTree[JsonNode](value._2))
        }

        resources.foreach {
          resource =>
              val rrepr = jacksonHalSerializer.valueToTree(makeRepr(resource._2, ec.hc, ec.rc.rc))
              mutnode.set(getLabel(resource._1), rrepr)
        }

        ec.repr
    }
  }

  private def fillSequence(ec: HalEntityContext, name: String, v: Traversable[_]): ResourceRepresentation[ObjectNode] = {
    serializeSequence(ec, v) match {
      case (resources, properties) =>
        if (properties.nonEmpty) {
          val arr = ec.repr.get.putArray(name)

          properties.foreach(v => arr.add(mapper.valueToTree[JsonNode](v)))
        }

        val reprWithCollection = ec.repr.withRel(Rels.collection(name))
        resources.foldLeft(reprWithCollection) {
          (acc, inner) =>
            acc.withRepresentation(name, inner)
        }
    }
  }

  private def serializeSequence(ec: HalEntityContext, sequence: Traversable[_]): (Seq[ResourceRepresentation[ObjectNode]], Seq[JsonNode]) = {
    sequence.partition(isHalResource) match {
      case (resources, properties) =>
        (
          resources.map(r => makeRepr(r, ec.hc, ec.rc.rc)).toSeq
          , properties.map(p => mapper.valueToTree[JsonNode](p)).toSeq
        )
    }
  }

  private def getLabel(value: Any): String =
    value match {
      case s: String => s
      case _ => mapper.convertValue[String](value)
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
}
