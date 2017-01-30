/*
 * Copyright (c) 2016. Teckro, Ltd.
 * All rights reserved.
 */

package org.bitbucket.pshirshov.izumitk.app.model

import scala.collection.mutable

trait Args

trait EPArguments {
  def entrypoint: String

  def entrypoint(value: String): this.type

  def entrypointArgs: mutable.Map[String, Args]

  def get[T](name: String): T = entrypointArgs(name).asInstanceOf[T]
}


