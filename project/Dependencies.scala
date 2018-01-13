package izumitk

import sbt.Keys._
import sbt._

object Dependencies {
  val Versions = Seq(
    crossScalaVersions := Seq("2.12.4", "2.11.11"),
    scalaVersion := crossScalaVersions.value.head
  )

//  val globalExclusions: Seq[SbtExclusionRule] = Seq[sbt.SbtExclusionRule]()


  object C {
    val scala_macros: ModuleID = "org.scalamacros" % "paradise" % "2.1.0"

    val jwt: ModuleID = "org.bitbucket.b_c" % "jose4j" % "0.4.4"

    val akka_version = "2.5.6"
    val akka_http_version = "10.0.10"
    private val akka_actor = "com.typesafe.akka" %% "akka-actor" % akka_version
    private val akka_slf4j = "com.typesafe.akka" %% "akka-slf4j" % akka_version
    private val akka_stream = "com.typesafe.akka" %% "akka-stream" % akka_version
    private val akka_http_dsl = "com.typesafe.akka" %% "akka-http" % akka_http_version
    val akka = Seq(akka_actor, akka_stream, akka_slf4j, akka_http_dsl)

    private val cassandra_core = "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.4"
    val cassandra: Seq[ModuleID] = Seq(cassandra_core).map(_.exclude("com.google.guava", "guava"))

    private val jackson_version = "2.8.7"
    private val jackson_databind = "com.fasterxml.jackson.core" % "jackson-databind" % jackson_version
    private val jackson_scala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % jackson_version
    private val jackson_jsr310 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jackson_version
    val jackson: Seq[ModuleID] = Seq(jackson_databind, jackson_scala, jackson_jsr310).map(_.exclude("com.google.guava", "guava"))
    val jsonpath: ModuleID = "com.jayway.jsonpath" % "json-path" % "2.2.0"

    private val scopt = "com.github.scopt" %% "scopt" % "3.7.0"
    private val jolokia = "org.jolokia" % "jolokia-core" % "1.3.7"
    private val jminix = "org.jminix" % "jminix" % "1.2.0"
    private val opendmk = "org.jvnet.opendmk" % "jmxremote_optional" % "1.0_01-ea"
    val essentials_app = Seq(scopt, jolokia, jminix, opendmk)

    private val jetty_version = "9.4.2.v20170220"
    private val jetty_server = "org.eclipse.jetty" % "jetty-server" % jetty_version
    private val jetty_servlet = "org.eclipse.jetty" % "jetty-servlet" % jetty_version
    private val netty_epoll = "io.netty" % "netty-transport-native-epoll" % "4.0.34.Final" classifier "linux-x86_64"
    val jetty = Seq(jetty_server, jetty_servlet, netty_epoll)

    private val guice_version = "4.1.0"
    private val guice_core = "com.google.inject" % "guice" % guice_version
    private val guice_multi = "com.google.inject.extensions" % "guice-multibindings" % guice_version
    private val guice_assisted = "com.google.inject.extensions" % "guice-assistedinject" % guice_version
    private val guice_grapher = "com.google.inject.extensions" % "guice-grapher" % guice_version
    private val guice_jmx = "com.google.inject.extensions" % "guice-jmx" % guice_version
    private val scala_guice = "net.codingwell" %% "scala-guice" % "4.1.0" exclude("com.google.code.findbugs", "jsr305")
    private val guice = Seq(
      guice_core
      , scala_guice
      , guice_multi
      , guice_assisted
      , guice_grapher
      , guice_jmx
    )

    private val commons_io = "commons-io" % "commons-io" % "2.5"
    private val commons_codec = "commons-codec" % "commons-codec" % "1.10"
    private val commons_lang3 = "org.apache.commons" % "commons-lang3" % "3.5"
    private val commons_math = "org.apache.commons" % "commons-math3" % "3.6.1"
    private val commons_compress: ModuleID = "org.apache.commons" % "commons-compress" % "1.14"

    val commons = Seq(commons_io, commons_lang3, commons_codec, commons_math, commons_compress)

    private val geoip_db: ModuleID = "com.maxmind.geoip2" % "geoip2" % "2.9.0"
    private val geoip_db_reader: ModuleID = "com.maxmind.db" % "maxmind-db" % "1.2.1"

    val geoip = Seq(geoip_db, geoip_db_reader)

    val apache_http: ModuleID = "org.apache.httpcomponents" % "httpclient" % "4.5.2"

    val scalactic_version = "3.0.1"
    private val scalactic = "org.scalactic" %% "scalactic" % scalactic_version

    private val config = "com.typesafe" % "config" % "1.3.1"
    private val logback = "ch.qos.logback" % "logback-classic" % "1.2.1"
    //private val janino = "org.codehaus.janino" % "janino" % "3.0.6"
    private val scala_logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
    private val scala_arm = "com.jsuereth" %% "scala-arm" % "2.0"
    private val slf4j_version = "1.7.24"
    private val slf4j_api = "org.slf4j" % "slf4j-api" % slf4j_version
    private val slf4j_log4j = "org.slf4j" % "log4j-over-slf4j" % slf4j_version
    private val jsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.1"
    //private val guava = "com.google.guava" % "guava" % "21.0"
    private val guava = "com.google.guava" % "guava" % "19.0" // c* driver depends on 16.0
    private val metrics_core = "io.dropwizard.metrics" % "metrics-core" % "3.1.2"

    val string_template: ModuleID = "org.antlr" % "ST4" % "4.0.8"

    val essentials: Seq[ModuleID] = (commons ++ guice ++ Seq(
      config
      , scala_logging
      , logback
      , scalactic
      , scala_arm
      , slf4j_api
      , slf4j_log4j
      , jsr305
      , metrics_core
      , string_template
    )).map(_.exclude("com.google.guava", "guava")) ++ Seq(guava)

    val halbuilder: ModuleID = "com.theoryinpractise" % "halbuilder-standard" % "4.0.1"

    val bouncycastle: ModuleID = "org.bouncycastle" % "bcprov-jdk16" % "1.46"
  }

  object CT {
    val akka_testkit: ModuleID = "com.typesafe.akka" %% "akka-testkit" % C.akka_version
    val akka_http_testkit: ModuleID = "com.typesafe.akka" %% "akka-http-testkit" % C.akka_http_version
    val scalatest: ModuleID = "org.scalatest" %% "scalatest" % C.scalactic_version
    val scalamock: ModuleID = ("org.scalamock" %% "scalamock-scalatest-support" % "3.5.0").exclude("org.scalatest", "scalatest_2.11")
  }

  object T {
    private val akka_testkit = CT.akka_testkit % "test"
    private val akka_http_testkit = CT.akka_http_testkit % "test"
    private val scalatest = CT.scalatest % "test"
    private val scalamock = CT.scalamock % "test"

    val essentials_test = Seq(scalatest, scalamock)
    val akka_test = Seq(T.akka_testkit, T.akka_http_testkit)
  }

}
