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

  case class WithConverter[T : TypeTag  : ClassTag : Manifest](entity: T, converter: HalConverter[T]) extends Hal {
    def entityClassTag = classTag[T]
    def entityTypeTag = classTag[T]
    def entityManifest = manifest[T]
  }


  case class Entity[T <: HalEntity : TypeTag : ClassTag : Manifest](entity: T) extends Hal  {
    def entityClassTag = classTag[T]
    def entityTypeTag = classTag[T]
    def entityManifest = manifest[T]
  }


  case class HalContext[T : TypeTag : ClassTag : Manifest](
                                                            dto: T
                                                            , baseUri: String
                                                            , repr: Representation) {
    def entityClassTag = classTag[T]
    def entityTypeTag = classTag[T]
    def entityManifest = manifest[T]
  }

  type HalHandler = HalContext[_] => Unit

  def emptyHandler: () => HalHandler = () => {
    _ =>
  }

  case class Auto[T : TypeTag : ClassTag : Manifest](entity: T, handler: HalHandler = emptyHandler()) extends Hal  {
    def entityClassTag = classTag[T]
    def entityTypeTag = classTag[T]
    def entityManifest = manifest[T]
  }
}