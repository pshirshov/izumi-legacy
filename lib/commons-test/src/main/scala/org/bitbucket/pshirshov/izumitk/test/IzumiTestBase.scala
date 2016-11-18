package org.bitbucket.pshirshov.izumitk.test

import org.scalamock.matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.{GivenWhenThen, Matchers, fixture}

/**
  *
  */
trait IzumiTestBase
  extends fixture.WordSpecLike
    with fixture.TestDataFixture
    with Matchers
    with GivenWhenThen
    with MockFactory
    with matchers.Matchers {

}
