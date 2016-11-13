package org.bitbucket.pshirshov.izumitk.app

import java.io.File


/**
  */
abstract class CommonOptionsParser[C <: WithBaseArguments](programName: String) extends scopt.OptionParser[C](programName) {
  head(programName, s"${Version.buildString()}", s"CWD=${System.getProperty("user.dir")}")
  help("help")
  version("version")

  opt[File]('c', "config") valueName "<config file>" action {
    case (x, c) =>
      c.baseCopy(c.base.copy(configFile = Some(x)))
  } text "configuration file"

  opt[File]('x', "logback") valueName "<logback.xml file>" action {
    case (x, c) =>
      c.baseCopy(c.base.copy(logbackFile = Some(x)))
  } text "path to logback configuration file"

  opt[Unit]("dump") abbr "d" action {
    (_, c) =>
      c.baseCopy(c.base.copy(dump = Some(true)))

  } text "dump effective configuration"

  opt[Unit]("reference") abbr "r" action {
    (_, c) =>
      c.baseCopy(c.base.copy(showReference = Some(true)))
  } text "show reference configuration"

  opt[Unit]("reference-startup") abbr "rs" action {
    (_, c) =>
      c.baseCopy(c.base.copy(allowReferenceStartup = Some(true)))
  } text "allow startup with builtin reference configuration"

  opt[Unit]("write-reference") abbr "wr" action {
    (_, c) =>
      c.baseCopy(c.base.copy(writeReference = Some(true)))
  } text "write reference configuration files to default paths. Note: requires `-rs` option!"

}
