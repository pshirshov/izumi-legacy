//logLevel := Level.Debug

// It's a fix for ugly SLF4J warning message
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"


resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"


addSbtPlugin("org.bitbucket.pshirshov.sbt" % "perfect-build" % "1.3.11")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")

