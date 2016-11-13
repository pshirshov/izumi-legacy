package org.bitbucket.pshirshov.izumitk.http.hal

import com.theoryinpractise.halbuilder.api.Representation

import scala.reflect._
import scala.reflect.runtime.universe._
/**
  */
sealed trait Hal

object Hal {
  type HalConverter[T] = (T, String) => Representation
  type HalHandler[T] = (T, String, Representation) => Unit

  case class Repr(representation: Representation) extends Hal

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

  def emptyHandler[T]: () => HalHandler[T] = () => {
    case (a, b, c) =>
  }

  case class Auto[T : TypeTag : ClassTag : Manifest](entity: T, handler: HalHandler[T] = emptyHandler[T]()) extends Hal  {
    def entityClassTag = classTag[T]
    def entityTypeTag = classTag[T]
    def entityManifest = manifest[T]
  }
}