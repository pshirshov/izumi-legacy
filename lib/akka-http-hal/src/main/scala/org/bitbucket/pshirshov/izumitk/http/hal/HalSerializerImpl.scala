package org.bitbucket.pshirshov.izumitk.http.hal

import java.util
import java.util.UUID

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.hal.HalResource
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import com.theoryinpractise.halbuilder.api._
import org.bitbucket.pshirshov.izumitk.http.hal.Hal.{HalContext, HalHandler}
import scala.collection.JavaConverters._

import scala.reflect.runtime.universe._


@Singleton
class HalSerializerImpl @Inject()
(
  @Named("standardMapper") mapper: JacksonMapper
  , factory: RepresentationFactory
) extends HalSerializer {
  override def makeRepr(
                         baseUri: String
                         , dto: Any
                         , handler: HalHandler
                       ): Representation = {
    val repr = factory.newRepresentation()

    dto match {
      case tree: ObjectNode =>
        serializeTree(tree, repr)
      case hr if hr.getClass.isAnnotationPresent(classOf[HalResource]) =>
        serializeDto(baseUri, dto, handler, repr)
      case _ =>
        throw new IllegalArgumentException(s"Not a HAL resource: $dto")
    }

    handler(HalContext(dto, baseUri, repr))
    repr
  }

  private def serializeDto(baseUri: String, dto: Any, handler: HalHandler, repr: Representation): Unit = {
    val rann = dto.getClass.getAnnotation(classOf[HalResource])
    Option(rann.self()).filter(_.nonEmpty).foreach {
      self =>
        repr.withLink("self", s"$baseUri/$self")
    }

    val rm = scala.reflect.runtime.currentMirror
    val instanceMirror = rm.reflect(dto)

    rm.classSymbol(dto.getClass).toType.members.collect {
      case m: MethodSymbol if m.isGetter && m.isPublic => m
    }.foreach {
      acc =>
        val name = acc.name.decodedName.toString
        val value = instanceMirror.reflectMethod(acc).apply()
        value match {
          case _: Number =>
            repr.withProperty(name, value)
          case _: String =>
            repr.withProperty(name, value)
          case _: UUID =>
            repr.withProperty(name, value.toString)
          case _: Boolean =>
            repr.withProperty(name, value)

          // TODO: improve maps support: we need to handle embedded hal resources as well
          case v: util.Map[_, _] =>
            fillMap(repr, baseUri, name, v.asScala.toMap, handler)

          case v: Map[_, _] =>
            fillMap(repr, baseUri, name, v, handler)

          case v: util.Collection[_] =>
            fillSequence(repr, baseUri, name, v.asScala.toSeq, handler)

          case v: Traversable[_] =>
            fillSequence(repr, baseUri, name, v.toSeq, handler)

          case v: AnyRef if isHalResource(v) =>
            repr.withRepresentation(name, makeRepr(baseUri, v, handler))

          case v: AnyRef => //if isHalProperty(v) =>
            repr.withProperty(name, mapper.valueToTree(v))

          case _ =>
            throw new UnsupportedOperationException(s"We don't know how to serialize `$value` in `$dto`")
        }
    }
  }

  private def fillMap(repr: Representation, baseUri: String, name: String, v: Map[_, _], handler: HalHandler): Any = {
    v.partition(pair => isHalResource(pair._2)) match {
      case (resources, values) =>
        val node = mapper.getNodeFactory.objectNode()

        resources.foreach {
          resource =>
            val rrepr = makeRepr(baseUri, resource._2, handler)
            val asJson = rrepr.toString(RepresentationFactory.HAL_JSON)
            node.set(resource._1.asInstanceOf[String], mapper.readValue[ObjectNode](asJson))
        }

        values.foreach {
          value =>
            node.set(value._1.asInstanceOf[String], mapper.valueToTree(value._2))
        }

        repr.withProperty(name, mapper.valueToTree(node))
    }
  }

  private def serializeTree(tree: ObjectNode, repr: Representation): Unit = {
    tree.fields().asScala.foreach {
      e =>
        repr.withProperty(e.getKey, e.getValue)
    }
  }

  private def fillSequence(repr: Representation, baseUri: String, name: String, v: Traversable[_], handler: HalHandler): Unit = {
    serializeSequence(v, baseUri, handler) match {
      case (resources, properties) =>
        if (properties.nonEmpty) {
          val arr = mapper.getNodeFactory.arrayNode()
          properties.foreach(v => arr.add(mapper.valueToTree[JsonNode](v)))
          repr.withProperty(name, arr)
        }

        resources.foreach(r => repr.withRepresentation(name, r))
    }
  }

  private def serializeSequence(sequence: Traversable[_], baseUri: String, handler: HalHandler): (Seq[Representation], Seq[JsonNode]) = {
    sequence.partition(isHalResource) match {
      case (resources, properties) =>
        (
          resources.map(r => makeRepr(baseUri, r, handler)).toSeq
          , properties.map(p => mapper.valueToTree[JsonNode](p)).toSeq
          )
    }
  }

  private def isHalResource(value: Any): Boolean = {
    value.getClass.isAnnotationPresent(classOf[HalResource])
  }

  //  private def isHalProperty(value: AnyRef): Boolean = {
  //    (Seq(value.getClass) ++ value.getClass.getInterfaces.toSeq)
  //      .flatMap(_.getAnnotations)
  //      .exists(_.annotationType() == classOf[HalProperty])
  //  }
}