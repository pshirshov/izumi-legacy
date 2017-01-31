package org.bitbucket.pshirshov.izumitk.app.model

import org.bitbucket.pshirshov.izumitk.config.LoadedConfig
import scopt.OptionParser

trait EntryPoint {
  def name: String

  def configure(parser: OptionParser[AppArguments]): Unit

  def run(args: AppArguments, config: LoadedConfig): Unit
}
