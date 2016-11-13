package org.bitbucket.pshirshov.izumitk.test

/**
  */
@ExposedTestScope
trait WithReusableResources {
  def getResource[R](name: String, constructor: () => R, destructor: (R) => Unit = {_: R =>}): R = {
    Option(ReusableHeavyTestResources.get[R](name)) match {
      case Some(resource) =>
        resource

      case None =>
        val resource: R = constructor()

        val wrapper = new ReusableTestResource[R] {
          override def get(): R = resource
          override def destroy(): Unit = destructor(resource)
        }

        ReusableHeavyTestResources.register(name, wrapper)
    }
  }
}
