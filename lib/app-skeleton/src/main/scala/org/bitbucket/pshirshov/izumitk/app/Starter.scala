package org.bitbucket.pshirshov.izumitk.app

import java.io.File

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.util.ContextInitializer
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import org.bitbucket.pshirshov.izumitk.config._
import org.apache.commons.io.{FileUtils, IOUtils}
import org.bitbucket.pshirshov.izumitk.app.model.AppModel._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}


/**
  */
abstract class Starter[ArgsType <: WithBaseArguments] {
  protected val referenceConfigName: String
  protected val defaultLogbackPath: String = Starter.defaultLogbackPath

  protected val parser: scopt.OptionParser[ArgsType]

  def handleConfigLoadingResult(loadedConfig: Try[LoadedConfig], args: ArgsType): LoadedConfig = {
    val config = loadedConfig.map {
      case c: LoadedResource =>
        println(s"Loaded REFERENCE resource. You should explicitly define config in filesystem. Here is your reference:")
        printReference(c)
        handleReference(args, c)

      case c: LoadedPath =>
        c

      case _ =>
        throw new IllegalArgumentException(s"Unexpected config")
    }

    config match {
      case Failure(t) =>
        println(s"Irrecoverable config loading error")
        t.printStackTrace()
        System.exit(1)
        throw new IllegalStateException()

      case Success(cfg) =>
        val fullConfig = cfg.effective.withFallback(ConfigFactory.load(referenceConfigName))
        ResolvedConfig(fullConfig, cfg.effectiveApp, cfg.reference)
    }
  }

  protected final def configuration(args: Array[String], defaults: ArgsType): StartupConfiguration[ArgsType] = {
    parser.parse(args, defaults) match {
      case Some(appArgs) =>
        parser.showHeader
        val cfg = config(appArgs)
        processParsedArguments(appArgs, cfg)
        StartupConfiguration(appArgs, cfg)

      case None =>
        parser.showUsage
        System.exit(0)
        throw new IllegalStateException()
    }
  }

  protected final def config(args: ArgsType): LoadedConfig = {
    val path = args.base.configFile match {
      case None =>
        referenceConfigName

      case Some(pathToConfig) =>
        pathToConfig.toPath.toString
    }

    //NamedConfigLoadingStrategy.init(path)
    FailingConfigLoadingStrategy.init()

    val loadedConfig = doLoadConfig(args, path)
    handleConfigLoadingResult(loadedConfig, args)
  }


  protected def doLoadConfig(args: ArgsType, path: String): Try[LoadedConfig] = {
    TypesafeConfigLoader.loadConfig(path, referenceConfigName)
  }

  protected def handleReference(args: ArgsType, c: LoadedConfig): LoadedConfig = {
    if (!args.base.allowReferenceStartup.get) {
      println(s"Exiting due to absence of explicitly defined config...")
      println(s"Use `--reference-startup` (`-rs`) option to override")
      System.exit(1)
      throw new IllegalStateException()
    } else {
      c
    }
  }

  protected def processParsedArguments(args: ArgsType, config: LoadedConfig) {
    args.base.dump match {
      case Some(true) =>
        printEffectiveConfig(config.effective)
        System.exit(0)
      case _ =>
    }

    args.base.showReference match {
      case Some(true) =>
        printReference(config)
        System.exit(0)
      case _ =>
    }

    args.base.writeReference match {
      case Some(true) =>
        writeReference(config)
        System.exit(0)
      case _ =>
    }

    args.base.logbackFile match {
      case Some(value) =>
        configureLogback(Some(value.getCanonicalPath))

      case _ =>
        if (logbackFile.exists()) {
          configureLogback(Some(logbackFile.getCanonicalPath))
        } else {
          println(s"Logback configuration $defaultLogbackPath not found, using reference...")
          configureLogback(None)
        }
    }
  }

  protected def logbackFile: File = {
    new File(defaultLogbackPath)
  }

  protected def printEffectiveConfig(config: Config): Unit = {
    val forDump = config
    val rendered = render(forDump)
    println("Config dump: \n" + rendered)
  }

  protected def printReference(c: LoadedConfig): Unit = {
    val toDump = c.reference
    val rendered = render(toDump)
    println("Reference config origin: " + toDump.origin)
    println("Reference config dump: \n" + rendered)
    println("Logging config dump: \n" + getLoggingReference)
  }


  protected def render(forDump: Config): String = {
    forDump.root.render(ConfigRenderOptions.defaults.setComments(false).setOriginComments(false).setJson(false))
  }

  protected def writeReference(c: LoadedConfig): Unit = {
    writeReference(new File(referenceConfigName), render(c.reference))
    writeReference(logbackFile, getLoggingReference)
  }

  protected def getLoggingReference: String = {
    IOUtils.toString(getClass.getResourceAsStream("/logback.xml"), "UTF-8")
  }

  protected def writeReference(target: File, content: String): Unit = {
    if (target.exists()) {
      Console.err.println(s"Can't overwrite existing config ${target.getCanonicalPath}, continuing...")
    } else {
      FileUtils.writeStringToFile(target, content)
      println(s"Reference config saved to ${target.getCanonicalPath}")
    }
  }

  protected def configureLogback(logbackXmlPath: Option[String]) {
    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    context.reset()

    try {
      val initializer = new ContextInitializer(context)
      logbackXmlPath match {
        case Some(path) =>
          val url = new File(path).toURI.toURL
          println(s"Initializing logback with $url...")
          initializer.configureByResource(url)
        case None =>
          println(s"Initializing logback with default settings...")
          initializer.autoConfig()
      }

    }
    catch {
      case je: JoranException =>

      case ex: Exception =>
        ex.printStackTrace()
    }
    StatusPrinter.printInCaseOfErrorsOrWarnings(context)
  }

  protected def safeMain(ep: => Unit): Unit = {
    try {
      ep
      System.exit(0)
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        System.exit(1)
    }
  }


}

object Starter {
  final val defaultLogbackPath = "logging.xml"
}
