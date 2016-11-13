package izumitk

import izumitk.Dependencies.{C, T}
import pbuild._
import sbt.Keys._
import sbt._

//import scoverage.ScoverageKeys._
import xerial.sbt.Sonatype.SonatypeKeys._

object IzumiBuild extends PerfectBuild {
  override lazy val allProjects: Map[String, Project] = Seq(
    // base libraries
    mkProject(
      base = file("lib/commons-test"),
      dependencies = Seq()
    ),
    mkProject(
      base = file("lib/commons-formats"),
      dependencies = Seq("commons-test")
    ),
    mkProject(
      base = file("lib/commons-config"),
      dependencies = Seq("commons-test")
    ),
    mkProject(
      base = file("lib/commons-cdi"),
      dependencies = Seq("commons-test", "commons-formats", "commons-config")
    ),
    mkProject(
      base = file("lib/app-skeleton"),
      dependencies = dep()
    ),
    mkProject(
      base = file("lib/json"),
      dependencies = dep()
    ),
    mkProject(
      base = file("lib/failures"),
      dependencies = dep()
    ),
    mkProject(
      base = file("lib/akka-cdi"),
      dependencies = dep()
    ),
    mkProject(
      base = file("lib/cassandra"),
      dependencies = dep(
        // may be moved to separate module
        "failures", "json"
      )
    ),
    mkProject(
      base = file("lib/rest-api"),
      dependencies = dep("json", "failures")
    )
  ).map(prj).toMap


  def essentials: Seq[sbt.ModuleID] = C.essentials

  def globalExclusions: Seq[sbt.SbtExclusionRule] = Dependencies.globalExclusions

  def scalaMacros: sbt.ModuleID = C.scala_macros

  def testEssentials: Seq[sbt.ModuleID] = T.essentials_test

  def versions: Seq[sbt.Def.Setting[_]] = Dependencies.Versions

  override def appDependencies: Seq[ClasspathDep[ProjectReference]] = dep("app-skeleton")


  override def publishSettings: Seq[_root_.sbt.Def.Setting[Task[Unit]]] = Seq(
    publish := { MultiPublishPlugin.MultiPublishSigned.value }
  )

  override lazy val baseSettings = super.baseSettings ++ Seq(
    //unmanagedBase := baseDirectory.value / "jars"
    resolvers += "restlet" at "http://maven.restlet.com/"
    , resolvers ++= config.publishing.map(_.resolver(isSnapshot.value)) // TODO: move to build
    //    , coverageOutputTeamCity := true
    //    , coverageOutputXML := true
    //    , coverageOutputHTML := true
    , sonatypeProfileName := "org.bitbucket.pshirshov.izumitk"
    , pomExtra := <url>https://bitbucket.org/pshirshov/scala-izumitk</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://www.opensource.org/licenses/bsd-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@bitbucket.org:pshirshov/scala-izumitk.git</url>
        <connection>scm:git@bitbucket.org:pshirshov/scala-izumitk.git</connection>
      </scm>
      <developers>
        <developer>
          <id>pshirshov</id>
          <name>Pavel Shirshov</name>
          <url>http://pshirshov.me</url>
        </developer>
      </developers>
  )
  // ScoverageKeys.coverageFailOnMinimum := false

  override lazy val defaultSettings = super.defaultSettings
}
