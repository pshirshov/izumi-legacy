package org.bitbucket.pshirshov.izumitk.app.model

import java.io.File

import scala.collection.mutable

case class AppArguments(
                         configFile: Option[File] = None
                         , logbackFile: Option[File] = None
                         , dump: Option[Boolean] = Option(false)
                         , allowReferenceStartup: Option[Boolean] = Option(false)
                         , showReference: Option[Boolean] = Option(false)
                         , writeReference: Option[Boolean] = Option(false)
                         , args: mutable.Map[String, AnyRef] = mutable.HashMap()
                         , toJson : Option[Boolean] = Option(false)
                         , full: Option[Boolean] = Option(false)
                       ) {
  def value[T](name: String): T = args(name).asInstanceOf[T]
  def get[T](name: String): Option[T] = args.get(name).map(_.asInstanceOf[T])
}
