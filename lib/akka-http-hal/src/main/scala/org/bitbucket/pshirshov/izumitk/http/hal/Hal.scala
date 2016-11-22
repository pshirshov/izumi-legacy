package org.bitbucket.pshirshov.izumitk.http.hal

import com.theoryinpractise.halbuilder.api.Representation

import scala.reflect._
import scala.reflect.runtime.universe._
/**
  */
sealed trait Hal

object Hal {
  case class Repr(representation: Representation) extends Hal


  type HalConverter[T] = (T, String) => Representation

  case class WithConverter[T : TypeTag  : Manifest](entity: T, converter: HalConverter[T]) extends Hal {
    def entityTypeTag: TypeTag[T] = typeTag[T]
    def entityManifest: Manifest[T] = manifest[T]
  }


  case class Entity[T <: HalEntity : TypeTag : Manifest](entity: T) extends Hal  {
    def entityTypeTag: TypeTag[T] = typeTag[T]
    def entityManifest: Manifest[T] = manifest[T]
  }


  case class HalContext[T : TypeTag : Manifest](
                                                            dto: T
                                                            , baseUri: String
                                                            , repr: Representation) {
    def entityTypeTag: TypeTag[T] = typeTag[T]
    def entityManifest: Manifest[T] = manifest[T]
  }

  type HalHandler = HalContext[_] => Unit

  def emptyHandler: () => HalHandler = () => {
    _ =>
  }

  case class Auto[T : TypeTag : Manifest](entity: T, handler: HalHandler = emptyHandler()) extends Hal  {
    def entityTypeTag: TypeTag[T] = typeTag[T]
    def entityManifest: Manifest[T] = manifest[T]
  }
}
