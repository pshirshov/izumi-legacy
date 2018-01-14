import izumitk.Dependencies.{C, T}
import org.bitbucket.pshirshov.izumi.sbt.IzumiImportsPlugin.autoImport.IzumiDsl._
import org.bitbucket.pshirshov.izumi.sbt.IzumiImportsPlugin.autoImport.IzumiScopes._
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

// TODO: library descriptor generator
// TODO: better analyzer for "exposed" scope
// TODO: config -- probably we don't need it
// TODO: conditionals in plugins: release settings, integration tests -- impossible

enablePlugins(ConvenienceTasksPlugin)

name := "izumi-legacy"

val AppSettings = SettingsGroupId()

resolvers in Global += "restlet" at "http://maven.restlet.com/"

javacOptions in Global -= "-Xdoclint:all"
scalacOptions in Global -= "-opt-warnings:_"
scalacOptions in Global -= "-Ywarn-extra-implicit"
scalacOptions in Global -= "-Ywarn-unused:_"
scalacOptions in Global -= "-Ypartial-unification"
pomExtra in Global := <url>https://bitbucket.org/pshirshov/izumi-legacy</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <developers>
    <developer>
      <id>pshirshov</id>
      <name>Pavel Shirshov</name>
      <url>https://github.com/pshirshov</url>
    </developer>
  </developers>

val baseSettings = new GlobalSettings {
  override val globalSettings: ProjectSettings = new ProjectSettings {
    override val settings = Seq(
      organization := "com.github.pshirshov.izumi.legacy"
      //, scalaVersion := "2.12.4"
      , crossScalaVersions := Seq(
        "2.12.4"
        , "2.11.11"
      )
      , publishMavenStyle in Global := true
      , sonatypeProfileName := "com.github.pshirshov"
      , publishTo := Some(
        if (isSnapshot.value)
          Opts.resolver.sonatypeSnapshots
        else
          Opts.resolver.sonatypeStaging
      )
      , credentials in Global += Credentials(new File("credentials.sonatype-nexus.properties"))
      
      , publishArtifact in(Test, packageBin) := true
      , publishArtifact in(Test, packageDoc) := true
      , publishArtifact in(Test, packageSrc) := true
      
      , releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies, // : ReleaseStep
        inquireVersions, // : ReleaseStep
        runClean, // : ReleaseStep
        runTest, // : ReleaseStep
        setReleaseVersion, // : ReleaseStep
        commitReleaseVersion, // : ReleaseStep, performs the initial git checks
        tagRelease, // : ReleaseStep
        //publishArtifacts,                       // : ReleaseStep, checks whether `publishTo` is properly set up
        setNextVersion, // : ReleaseStep
        commitNextVersion, // : ReleaseStep
        pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
      )
    )

    override val sharedDeps = Seq(
      T.essentials_test
      , C.essentials
    ).flatten.toSet
  }
}

// --------------------------------------------
val globalDefs = setup(baseSettings)
// --------------------------------------------

val inRoot = In(".")
val inLib = In("lib")

val commonsTest = inLib.as.module
val commonsModel = inLib.as.module
    .depends(commonsTest.testOnlyRef)
val commonsUtil = inLib.as.module
  .depends(commonsTest.testOnlyRef, commonsModel)
val commonsConfig = inLib.as.module
  .depends(commonsTest.testOnlyRef)
val commonsCdi = inLib.as.module
  .depends(commonsTest.testOnlyRef, commonsUtil, commonsConfig)

val sharedDefs = globalDefs.withSharedLibs(
  commonsCdi
  , commonsModel
  , commonsUtil
  , commonsConfig
  , commonsTest.testOnlyRef
)

val failuresModel = inLib.as.module
val failures = inLib.as.module
  .depends(failuresModel)
val json = inLib.as.module

val akka = inLib.as.module
val akkaCdi = inLib.as.module
  .depends(akka)
val akkaHttp = inLib.as.module
  .depends(akka, json)
val akkaHttpHal = inLib.as.module
  .depends(akkaHttp, failures)
val akkaHttpRestlike = inLib.as.module
  .depends(akkaHttp, failures)



val appSkeleton = inLib.as.module

val geoip = inLib.as.module
  .depends(akkaHttp, failures)

val appAkkaHttp = inLib.as.module
  .depends(appSkeleton, akkaHttp)

val cassandra = inLib.as.module
val clustering = inLib.as.module

val failuresCassandra = inLib.as.module
  .depends(cassandra, failures, json)


lazy val root = inRoot.as
  .root
  .enablePlugins(GitStampPlugin)
  .transitiveAggregate(
    json
    , akkaHttpHal, akkaHttpRestlike, akkaCdi, appAkkaHttp
    , appSkeleton
    , cassandra
    , clustering
    , failuresCassandra
    , geoip
  )

