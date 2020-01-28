package com.africasTalking.egan.core
package db.test

import scala.language.postfixOps

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }

import com.africasTalking._

import egan.core.util.EganCoreServiceT

abstract class EganCoreTestServiceT extends TestKit(ActorSystem("MyTestSystem"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with EganCoreServiceT {

  import akka.testkit.TestKit

  override def snoopServiceName = "TestService"
  override def actorRefFactory  = system

  override def beforeAll {
    Thread.sleep(3000)
  }

  override def afterAll {
    Thread.sleep(3000)
    TestKit.shutdownActorSystem(system)
  }
}
