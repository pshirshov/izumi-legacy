package org.bitbucket.pshirshov.izumitk.app.util

import java.io.File

import org.bitbucket.pshirshov.izumitk.app.Version
import org.bitbucket.pshirshov.izumitk.app.model.AppArguments


/**
  */
abstract class CommonOptionsParser(programName: String) extends scopt.OptionParser[AppArguments](programName) {
  head(programName, s"${Version.buildString()}", s"CWD=${System.getProperty("user.dir")}")
  help("help")
  version("version")

  opt[File]('c', "config") valueName "<config file>" action {
    case (x, c) =>
      c.copy(configFile = Some(x))
  } text "configuration file"

  opt[File]('x', "logback") valueName "<logback.xml file>" action {
    case (x, c) =>
      c.copy(logbackFile = Some(x))
  } text "path to logback configuration file"

  opt[Unit]("dump") abbr "d" action {
    (_, c) =>
      c.copy(dump = Some(true))

  } text "dump effective configuration"

  opt[Unit]("reference") abbr "r" action {
    (_, c) =>
      c.copy(showReference = Some(true))
  } text "show reference configuration"

  opt[Unit]("reference-startup") abbr "rs" action {
    (_, c) =>
      c.copy(allowReferenceStartup = Some(true))
  } text "allow startup with builtin reference configuration"

  opt[Unit]("write-reference") abbr "wr" action {
    (_, c) =>
      c.copy(writeReference = Some(true))
  } text "write reference configuration files to default paths. Note: requires `-rs` option!"

}
