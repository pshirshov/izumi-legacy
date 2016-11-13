package org.bitbucket.pshirshov.izumitk.test

import org.scalamock.scalatest.MockFactory
import org.scalatest.{GivenWhenThen, fixture}

/**
  *
  */
trait IzumiTestBase
  extends fixture.WordSpecLike
    with MockFactory
    with fixture.TestDataFixture
    with GivenWhenThen {

}
