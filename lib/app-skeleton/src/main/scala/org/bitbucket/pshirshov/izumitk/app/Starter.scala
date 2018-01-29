package org.bitbucket.pshirshov.izumitk.app

import java.io.File
import java.nio.charset.StandardCharsets

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.util.ContextInitializer
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions, ConfigRenderOptions}
import org.apache.commons.io.{FileUtils, IOUtils}
import org.bitbucket.pshirshov.izumitk.app.model.{AppArguments, StartupConfiguration}
import org.bitbucket.pshirshov.izumitk.config._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}


/**
  */
abstract class Starter {
  protected val referenceConfigName: String
  protected val defaultLogbackPath: String = Starter.defaultLogbackPath

  protected val parser: scopt.OptionParser[AppArguments]

  def handleConfigLoadingResult(loadedConfig: Try[LoadedConfig], args: AppArguments): LoadedConfig = {
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

  protected final def configuration(args: Array[String], defaults: AppArguments): StartupConfiguration = {
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

  protected final def config(args: AppArguments): LoadedConfig = {
    val path = args.configFile match {
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


  protected def doLoadConfig(args: AppArguments, path: String): Try[LoadedConfig] = {
    TypesafeConfigLoader.loadConfig(path, referenceConfigName)
  }

  protected def handleReference(args: AppArguments, c: LoadedConfig): LoadedConfig = {
    if (!args.allowReferenceStartup.get) {
      println(s"Exiting due to absence of explicitly defined config...")
      println(s"Use `--reference-startup` (`-rs`) option to override")
      System.exit(1)
      throw new IllegalStateException()
    } else {
      c
    }
  }

  protected def processParsedArguments(args: AppArguments, config: LoadedConfig): Unit = {
    args.dump match {
      case Some(true) =>
        printEffectiveConfig(config.effective)
        System.exit(0)
      case _ =>
    }

    args.showReference match {
      case Some(true) =>
        printReference(config)
        System.exit(0)
      case _ =>
    }

    args.writeReference match {
      case Some(true) =>
        writeReference(config, args.toJson.get, args.full.get)
        System.exit(0)
      case _ =>
    }

    args.logbackFile match {
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


  protected def render(forDump: Config, toJson : Boolean = false): String = {
    forDump.root.render(ConfigRenderOptions.defaults.setComments(false).setOriginComments(false).setJson(toJson))
  }

  protected def writeReference(c: LoadedConfig, jsonFormat : Boolean, fullConfig: Boolean): Unit = {
    val fileName = if (jsonFormat) referenceConfigName.replaceFirst(".conf", ".json") else referenceConfigName
    val config = if (fullConfig) fullRefferenceConfig() else c.reference
    writeReference(new File(fileName), render(config, jsonFormat))
    writeReference(logbackFile, getLoggingReference)
  }

  protected def fullRefferenceConfig(): Config = {
    import scala.collection.JavaConverters._

    val appConf = ConfigFactory.parseResourcesAnySyntax(
      referenceConfigName, ConfigParseOptions.defaults().setAllowMissing(true)
    )
    
    val systemProps = ConfigFactory.defaultOverrides.root.unwrapped.asScala.keySet
    val withoutSystemReference = ConfigFactory.parseMap(
      appConf
        .withFallback(ConfigFactory.defaultReference(classOf[Object].getClassLoader))
        .root().asScala
        .filterKeys(!systemProps.contains(_))
        .toMap.asJava
    )
    
    appConf.withFallback(withoutSystemReference)
  }

  protected def getLoggingReference: String = {
    IOUtils.toString(getClass.getResourceAsStream("/logback.xml"), "UTF-8")
  }

  protected def writeReference(target: File, content: String): Unit = {
    if (target.exists()) {
      Console.err.println(s"Can't overwrite existing config ${target.getCanonicalPath}, continuing...")
    } else {
      FileUtils.writeStringToFile(target, content, StandardCharsets.UTF_8)
      println(s"Reference config saved to ${target.getCanonicalPath}")
    }
  }

  protected def configureLogback(logbackXmlPath: Option[String]): Unit = {
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
