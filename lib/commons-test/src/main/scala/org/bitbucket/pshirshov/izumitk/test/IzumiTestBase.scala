package org.bitbucket.pshirshov.izumitk.test

import org.scalamock
import org.scalamock.proxy
import org.scalamock.proxy.ProxyMockFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.{GivenWhenThen, Matchers, Suite, fixture}

import scala.reflect.ClassTag

/**
  *
  */
trait WithScalamock
  extends MockFactory
    with scalamock.matchers.Matchers {
  this: Suite =>

  object Proxy extends ProxyMockFactory {
    def mock[T: ClassTag]: T with proxy.Mock = super.mock[T]

    def stub[T: ClassTag]: T with proxy.Stub = super.stub[T]
  }

}

trait IzumiTestBase
  extends fixture.WordSpecLike
    with fixture.TestDataFixture
    with Matchers
    with GivenWhenThen {

}
