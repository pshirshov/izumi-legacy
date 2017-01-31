package org.bitbucket.pshirshov.izumitk.modularity

trait WithPluginsPackages {
  protected def appId: String

  protected def namespace: String = "plugins"

  protected def basePackage: Package = getClass.getPackage

  protected def companyPackage(): String = basePackage.getName.split('.').take(2).toList.mkString(".")

  protected def classPackage(): String = basePackage.getName

  protected def pluginsPackages(): Seq[String] = {
    withPkg(namespace, WithPluginsPackages.izumiPackages() ++ appPackages()).distinct
  }

  protected final def withPkg(subPkg: String, packages: Seq[String]): Seq[String] = packages.map(p => s"$p.$subPkg")

  protected def appPackages(): Seq[String] = {
    val pkgCompany = companyPackage()
    val pkgClass = classPackage()

    Seq(
      s"$pkgCompany"
      , s"$pkgCompany.$appId"
      , s"$pkgClass"
    )
  }
}

object WithPluginsPackages {
  def izumiPackages(): Seq[String] = {
    Seq(
      s"izumi"
      , s"izumitk"
      , s"org.bitbucket.pshirshov.izumitk"
    )
  }
}
