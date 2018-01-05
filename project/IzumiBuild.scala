//package izumitk
//
//import izumitk.Dependencies.{C, T}
//import pbuild._
//import sbt.Keys._
//import sbt._
//
//import xerial.sbt.Sonatype.SonatypeKeys._
//
//object IzumiBuild extends PerfectBuild {
//  override lazy val allProjects: Map[String, Project] = Seq(
//    // common tools
//    mkProject(
//      base = file("lib/commons-test"),
//      dependencies = Seq()
//    )
//    , mkProject(
//      base = file("lib/commons-model"),
//      dependencies = customDep(allProjects("commons-test") % "test")
//    )
//    , mkProject(
//      base = file("lib/commons-util"),
//      dependencies = customDep(allProjects("commons-test") % "test", allProjects("commons-model"))
//    )
//    , mkProject(
//      base = file("lib/commons-config"),
//      dependencies = customDep(allProjects("commons-test") % "test")
//    )
//    , mkProject(
//      base = file("lib/commons-cdi"),
//      dependencies = customDep(allProjects("commons-test") % "test"
//        , "commons-util"
//        , allProjects("commons-config") % "compile->compile;test->compile,test"
//      )
//    )
//    // generic libraries
//    , mkProject(
//      base = file("lib/json"),
//      dependencies = dep()
//    )
//    , mkProject(
//      base = file("lib/failures-model"),
//      dependencies = dep()
//    )
//    , mkProject(
//      base = file("lib/failures"),
//      dependencies = dep("failures-model")
//    )
//    , mkProject(
//      base = file("lib/app-skeleton"),
//      dependencies = dep()
//    )
//    , mkProject(
//      base = file("lib/clustering"),
//      dependencies = dep()
//    )
//    , mkProject(
//      base = file("lib/geoip"),
//      dependencies = dep("failures-model", "akka-http")
//    )
//    , mkProject(
//      base = file("lib/akka"),
//      dependencies = dep()
//    )
//    , mkProject(
//      base = file("lib/cassandra"),
//      dependencies = dep()
//    )
//    , mkProject(
//      base = file("lib/failures-cassandra"),
//      dependencies = dep("cassandra", "failures", "json")
//    )
//    , mkProject(
//      base = file("lib/akka-cdi"),
//      dependencies = dep("akka")
//    )
//    , mkProject(
//      base = file("lib/akka-http"),
//      dependencies = dep("akka", "json")
//    )
//    , mkProject(
//      base = file("lib/akka-http-restlike"),
//      dependencies = dep("akka-http", "failures")
//    )
//    , mkProject(
//      base = file("lib/akka-http-hal"),
//      dependencies = dep("akka-http", "failures")
//    )
//    ,mkProject(
//      base = file("lib/app-akka-http"),
//      dependencies = dep("app-skeleton", "akka-http")
//    )
//
//
//  ).map(prj).toMap
//
//
//  def essentials: Seq[sbt.ModuleID] = C.essentials
//
//  def globalExclusions: Seq[sbt.SbtExclusionRule] = Dependencies.globalExclusions
//
//  def scalaMacros: sbt.ModuleID = C.scala_macros
//
//  def testEssentials: Seq[sbt.ModuleID] = T.essentials_test
//
//  def versions: Seq[sbt.Def.Setting[_]] = Dependencies.Versions
//
//  override def appDependencies: Seq[ClasspathDep[ProjectReference]] = dep("app-skeleton")
//
//
//  override def publishSettings: Seq[_root_.sbt.Def.Setting[_]] = Seq(
//    publish := {
//      MultiPublishPlugin.MultiPublishSigned.value
//    }
//    , publishArtifact in(Test, packageBin) := true
//    , publishArtifact in(Test, packageDoc) := true
//    , publishArtifact in(Test, packageSrc) := true
//  )
//
//  override lazy val baseSettings: Seq[_root_.sbt.Def.Setting[_]] = super.baseSettings ++ Seq(
//    //unmanagedBase := baseDirectory.value / "jars"
//    VersioningPlugin.Keys.releaseBranch := {
//      Some("release")
//    }
//    , resolvers += "restlet" at "http://maven.restlet.com/"
//    , resolvers ++= config.publishing.map(_.resolver(isSnapshot.value)) // TODO: move to build
//    //    , coverageOutputTeamCity := true
//    //    , coverageOutputXML := true
//    //    , coverageOutputHTML := true
//    , sonatypeProfileName := "org.bitbucket.pshirshov"
//    , pomExtra := <url>https://bitbucket.org/pshirshov/scala-izumitk</url>
//      <licenses>
//        <license>
//          <name>BSD-style</name>
//          <url>http://www.opensource.org/licenses/bsd-license.php</url>
//          <distribution>repo</distribution>
//        </license>
//      </licenses>
//      <scm>
//        <url>git@bitbucket.org:pshirshov/scala-izumitk.git</url>
//        <connection>scm:git@bitbucket.org:pshirshov/scala-izumitk.git</connection>
//      </scm>
//      <developers>
//        <developer>
//          <id>pshirshov</id>
//          <name>Pavel Shirshov</name>
//          <url>http://pshirshov.me</url>
//        </developer>
//      </developers>
//  )
//  // ScoverageKeys.coverageFailOnMinimum := false
//
//  override lazy val defaultSettings: Seq[_root_.sbt.Def.Setting[_]] = super.defaultSettings
//}
