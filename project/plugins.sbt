scalaVersion := "2.12.4"

resolvers += Opts.resolver.sonatypeReleases

// https://github.com/coursier/coursier#sbt-plugin
addSbtPlugin("io.get-coursier" % "sbt-coursier" % sys.props.get("build.coursier.version").getOrElse("1.0.0"))

// https://github.com/pshirshov/izumi-r2
addSbtPlugin("com.github.pshirshov.izumi.r2" %% "sbt-izumi" % "0.4.16")
