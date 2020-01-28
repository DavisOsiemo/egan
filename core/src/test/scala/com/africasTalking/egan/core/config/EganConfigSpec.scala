package com.africasTalking.egan.core
package config

import org.scalatest.flatspec.AnyFlatSpec

import io.atlabs._

import horus.core.util.ATEnum.ATEnvironment

class EganConfigSpec extends AnyFlatSpec {

  object TestDevConfig extends EganConfigT {
    override protected def getEnvironmentImpl = "dev"
  }

  object TestStagingConfig extends EganConfigT {
    override protected def getEnvironmentImpl = "staging"
  }

  object TestSandboxConfig extends EganConfigT {
    override protected def getEnvironmentImpl = "sandbox"
  }

  object TestProdConfig extends EganConfigT {
    override protected def getEnvironmentImpl = "prod"
  }

  "The ATConfig" should "come up correctly" in {
    assert(TestDevConfig.getEnvironment     == ATEnvironment.Development)
    assert(TestStagingConfig.getEnvironment == ATEnvironment.Staging)
    assert(TestSandboxConfig.getEnvironment == ATEnvironment.Sandbox)
    assert(TestProdConfig.getEnvironment    == ATEnvironment.Production)
  }
}
