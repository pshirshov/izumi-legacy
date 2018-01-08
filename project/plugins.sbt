scalaVersion := "2.12.4"

// https://github.com/coursier/coursier#sbt-plugin
addSbtPlugin("io.get-coursier" % "sbt-coursier" % sys.props.get("build.coursier.version").getOrElse("1.0.0"))

addSbtPlugin("com.github.pshirshov.izumi" %% "sbt-izumi" % "0.4.11")

////logLevel := Level.Debug
//
//// It's a fix for ugly SLF4J warning message
//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"
//
//
//resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
//
//
//addSbtPlugin("org.bitbucket.pshirshov.sbt" % "perfect-build" % "1.3.38")
//addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")
//
//val credsFile = new File("credentials.teckro-nexus.properties")
//
//resolvers ++= (if (credsFile.exists()) {
//  Seq("Teckro Nexus Releases" at "https://nexus.teckro.com/nexus/content/repositories/releases/")
//} else {
//  Seq()
//})
//
//
//credentials ++= (if (credsFile.exists()) {
//  Seq(Credentials(credsFile))
//} else {
//  Seq()
//})
