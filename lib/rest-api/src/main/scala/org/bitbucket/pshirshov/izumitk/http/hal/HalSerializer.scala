package org.bitbucket.pshirshov.izumitk.http.hal

import java.util
import java.util.UUID

import com.fasterxml.jackson.databind.JsonNode
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import org.bitbucket.pshirshov.izumitk.hal.HalResource
import org.bitbucket.pshirshov.izumitk.json.JacksonMapper
import com.theoryinpractise.halbuilder.api._

import scala.reflect.runtime.universe._
/**
  */
@Singleton
class HalSerializer @Inject()(
  @Named("standardMapper") mapper: JacksonMapper
  , factory: RepresentationFactory
) {
  def makeRepr(baseUri: String, dto: AnyRef): Representation = {
    if (!dto.getClass.isAnnotationPresent(classOf[HalResource])) {
      throw new IllegalArgumentException(s"Not a HAL resource: $dto")
    }

    val repr = factory.newRepresentation()
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
          case _:java.lang.Number =>
            repr.withProperty(name, value)
          case _: String =>
            repr.withProperty(name, value)
          case _: UUID =>
            repr.withProperty(name, value.toString)
          case _: Boolean =>
            repr.withProperty(name, value)
          case v: util.Map[_, _] =>
            repr.withProperty(name, mapper.valueToTree(v))
          case v: util.Collection[AnyRef] =>
            import scala.collection.JavaConversions._
            fillSequence(repr, baseUri, name, v.toSeq)
          case v: Traversable[AnyRef] =>
            fillSequence(repr, baseUri, name, v.toSeq)
          case v: AnyRef if isHalResource(v) =>
            repr.withRepresentation(name, makeRepr(baseUri, v))
          case v: AnyRef => //if isHalProperty(v) =>
            repr.withProperty(name, mapper.valueToTree(v))
          case _ =>
            throw new UnsupportedOperationException(s"We don't know how to serialize `$value` in `$dto`")
        }
    }

    repr
  }

  private def fillSequence(repr: Representation, baseUri: String, name: String, v: Traversable[AnyRef]): Unit = {
    serializeSequence(v, baseUri) match {
      case (resources, properties) =>
        if (properties.nonEmpty) {
          val arr = mapper.getNodeFactory.arrayNode()
          properties.foreach(v => arr.add(mapper.valueToTree[JsonNode](v)))
          repr.withProperty(name, arr)
        }

        resources.foreach(r => repr.withRepresentation(name, r))
    }
  }

  private def serializeSequence(sequence: Traversable[AnyRef], baseUri: String): (Seq[Representation], Seq[JsonNode]) = {
    sequence.partition(isHalResource) match {
      case (resources, properties) =>
        (
          resources.map(r => makeRepr(baseUri, r)).toSeq
          , properties.map(p => mapper.valueToTree[JsonNode](p)).toSeq
          )
    }
  }

  private def isHalResource(value: AnyRef): Boolean = {
    value.getClass.isAnnotationPresent(classOf[HalResource])
  }

//  private def isHalProperty(value: AnyRef): Boolean = {
//    (Seq(value.getClass) ++ value.getClass.getInterfaces.toSeq)
//      .flatMap(_.getAnnotations)
//      .exists(_.annotationType() == classOf[HalProperty])
//  }
}