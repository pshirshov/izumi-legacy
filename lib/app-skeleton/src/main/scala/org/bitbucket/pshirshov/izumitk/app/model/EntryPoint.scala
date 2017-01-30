package org.bitbucket.pshirshov.izumitk.app.model

import org.bitbucket.pshirshov.izumitk.config.LoadedConfig
import scopt.OptionParser

trait EntryPoint {
  def name: String

  def configure[T <: EPArguments](parser: OptionParser[T]): Unit

  def run(args: EPArguments, config: LoadedConfig): Unit
}
