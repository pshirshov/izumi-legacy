package izumitk

import sbt.Keys._
import sbt._

object Dependencies {
  val scala_version = "2.11.8"
  val Versions = Seq(
    crossScalaVersions := Seq(scala_version),
    scalaVersion := crossScalaVersions.value.head
  )

  val globalExclusions = Seq[sbt.SbtExclusionRule]()

//  val totalExclusions = Seq(ExclusionRule("com.google.guava", "guava")
//  , ExclusionRule("com.typesafe.akka", "akka-actor_2.11")
//  , ExclusionRule("com.fasterxml.jackson.core", "jackson-annotations")
//  , ExclusionRule("com.typesafe", "config")
//  , ExclusionRule("org.slf4j", "slf4j-api"))

  object C {
    val scala_macros = "org.scalamacros" % "paradise" % "2.1.0"

    val json4s = "org.json4s" %% "json4s-native" % "3.3.0"
    val jwt = "org.bitbucket.b_c" % "jose4j" % "0.4.4"
    val restfb = "com.restfb" % "restfb" % "1.19.0"

    val akka_version = "2.4.11"
    private val akka_actor = "com.typesafe.akka" %% "akka-actor" % akka_version
    private val akka_slf4j = "com.typesafe.akka" %% "akka-slf4j" % akka_version
    private val akka_stream = "com.typesafe.akka" %% "akka-stream" % akka_version
    private val akka_http = "com.typesafe.akka" %% "akka-http-core" % akka_version
    private val akka_http_dsl = "com.typesafe.akka" %% "akka-http-experimental" % akka_version
    //private val akka_http_xml = "com.typesafe.akka" %% "akka-http-xml-experimental" % akka_version
    val akka = Seq(akka_actor, akka_slf4j, akka_http_dsl)

    private val cassandra_core = "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.0"
    val cassandra = Seq(cassandra_core).map(_.exclude("com.google.guava", "guava"))

    val hikaricp = "com.zaxxer" % "HikariCP" % "2.5.1"
    val clickhouse = "ru.yandex.clickhouse" % "clickhouse-jdbc" % "0.1.11"

    private val jackson_version = "2.8.1"
    private val jackson_databind = "com.fasterxml.jackson.core" % "jackson-databind" % jackson_version
    private val jackson_scala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % jackson_version
    private val jackson_jsr310 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jackson_version
    val jackson = Seq(jackson_databind, jackson_scala, jackson_jsr310).map(_.exclude("com.google.guava", "guava"))

    private val scopt = "com.github.scopt" %% "scopt" % "3.3.0"
    private val jolokia = "org.jolokia" % "jolokia-core" % "1.3.2"
    private val jminix = "org.jminix" % "jminix" % "1.2.0"
    private val opendmk = "org.jvnet.opendmk" % "jmxremote_optional" % "1.0_01-ea"
    val essentials_app = Seq(scopt, jolokia, jminix, opendmk)

    private val jetty_version = "9.3.8.v20160314"
    private val jetty_server = "org.eclipse.jetty" % "jetty-server" % jetty_version
    private val jetty_servlet = "org.eclipse.jetty" % "jetty-servlet" % jetty_version
    private val netty_epoll = "io.netty" % "netty-transport-native-epoll" % "4.0.34.Final" classifier "linux-x86_64"
    val jetty = Seq(jetty_server, jetty_servlet, netty_epoll)

    private val guice_version = "4.1.0"
    private val guice_core = "com.google.inject" % "guice" % guice_version
    private val guice_multi = "com.google.inject.extensions" % "guice-multibindings" % guice_version
    private val guice_assisted = "com.google.inject.extensions" % "guice-assistedinject" % guice_version
    private val scala_guice = "net.codingwell" %% "scala-guice" % "4.0.0" exclude("com.google.code.findbugs", "jsr305")
    private val guice = Seq(guice_core, guice_multi, guice_assisted, scala_guice)

    private val commons_io = "commons-io" % "commons-io" % "2.4"
    private val commons_codec = "commons-codec" % "commons-codec" % "1.10"
    private val commons_lang3 = "org.apache.commons" % "commons-lang3" % "3.4"
    private val commons_math = "org.apache.commons" % "commons-math3" % "3.5"
    val commons = Seq(commons_io, commons_lang3, commons_codec, commons_math)

    val scalactic_version = "3.0.0-M15" // more recent versions are breaking scalamock
    private val scalactic = "org.scalactic" %% "scalactic" % scalactic_version

    private val config = "com.typesafe" % "config" % "1.3.1"
    private val logback = "ch.qos.logback" % "logback-classic" % "1.1.7"
    private val janino = "org.codehaus.janino" % "janino" % "2.7.8"
    private val scala_logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
    private val scala_arm = "com.jsuereth" %% "scala-arm" % "2.0-RC1"
    private val slf4j_api = "org.slf4j" % "slf4j-api" % "1.7.21"
    private val slf4j_log4j = "org.slf4j" % "log4j-over-slf4j" % "1.7.21"
    private val jsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.0"
    private val guava = "com.google.guava" % "guava" % "19.0"
    private val metrics_core =  "io.dropwizard.metrics" % "metrics-core" % "3.1.2"

    private val apache_camel_version = "2.17.1"
    private val apache_camel_core = "org.apache.camel" % "camel-core" % apache_camel_version
    private val apache_camel_scala = "org.apache.camel" % "camel-scala" % apache_camel_version
    private val apache_camel_guice = "org.apache.camel" % "camel-guice" % apache_camel_version
    private val apache_camel_stream = "org.apache.camel" % "camel-stream" % apache_camel_version
    private val apache_camel_kafka = "org.apache.camel" % "camel-kafka" % apache_camel_version
    val apache_camel = Seq(apache_camel_core, apache_camel_scala, apache_camel_guice, apache_camel_stream, apache_camel_kafka)

    private val apache_kafka_version = "0.10.0.0"
    private val apache_kafka_core = "org.apache.kafka" % "kafka_2.11" % apache_kafka_version
    private val apache_kafka_clients = "org.apache.kafka" % "kafka-clients" % apache_kafka_version
    val apache_kafka = Seq(apache_kafka_core, apache_kafka_clients).map(
      _.exclude("org.slf4j", "slf4j-log4j12") // fixing slf4j bindings
        // fixing kafka. See: https://issues.apache.org/jira/browse/KAFKA-974
      .exclude("javax.jms", "jms")
      .exclude("com.sun.jdmk", "jmxtools")
      .exclude("com.sun.jmx", "jmxri")
      .exclude("log4j", "log4j")
    )

    val apache_commons_validator = "commons-validator" % "commons-validator" % "1.5.1"

    val string_template = "org.antlr" % "ST4" % "4.0.8"

    val essentials = (commons ++ guice ++ Seq(
      config
      , scala_logging
      , logback
      , janino
      , scalactic
      , scala_arm
      , slf4j_api
      , slf4j_log4j
      , jsr305
      , metrics_core
      , string_template
    )).map(_.exclude("com.google.guava", "guava")) ++ Seq(guava)

    private val gatling_version = "2.2.0"
    private val gatling_app = "io.gatling" % "gatling-app" % gatling_version
    private val gatling_recorder = "io.gatling" % "gatling-recorder" % gatling_version
    private val gatling_http = "io.gatling" % "gatling-http" % gatling_version
    private val gatling_highcharts = "io.gatling.highcharts" % "gatling-charts-highcharts" % gatling_version
    val gatling = Seq(gatling_app, gatling_http, gatling_recorder, gatling_highcharts)

    val elasticsearch = "org.elasticsearch" % "elasticsearch" % "2.3.4"

    val activemq = "org.apache.activemq" % "activemq-client" % "5.14.1"

    val halbuilder = "com.theoryinpractise" % "halbuilder-standard" % "4.0.1"
  }

  object CT {
    val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % C.akka_version
    val akka_http_testkit = "com.typesafe.akka" %% "akka-http-testkit" % C.akka_version
    val scalatest = "org.scalatest" %% "scalatest" % C.scalactic_version
    val scalamock = ("org.scalamock" %% "scalamock-scalatest-support" % "3.2.2").exclude("org.scalatest", "scalatest_2.11")
    val htmlunit = "net.sourceforge.htmlunit" % "htmlunit" % "2.23"
  }

  object T {
    private val akka_testkit = CT.akka_testkit % "test"
    private val akka_http_testkit = CT.akka_http_testkit % "test"
    private val scalatest = CT.scalatest % "test"
    private val scalamock = CT.scalamock % "test"

    val htmlunit = CT.htmlunit % "test"

    val essentials_test = Seq(scalatest, scalamock)
    val akka_test = Seq(T.akka_testkit, T.akka_http_testkit)
  }

}
