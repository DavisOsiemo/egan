package com.africasTalking.egan.core
package db.mysql.service

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.Props

import com.africasTalking._

import egan.core.db.mysql.EganMySqlDbService.{ UserDbEntry, UserFetchDbQuery }
import egan.core.db.mysql.EganMySqlDbService
import egan.core.db.test.EganCoreTestServiceT

class MysqlDbServiceSpec extends EganCoreTestServiceT {
  val dbService   = system.actorOf(Props[EganMySqlDbService])
  val testUserId                    = 1
  val testUsername                  = "Hungry1"
  "EganMysqlDbService" must {
    "fetch all users correctly" in {
      dbService ! UserFetchDbQuery
      val allUsers = expectMsgClass(
        10 seconds,
        classOf[List[UserDbEntry]]
      )
    }
  }
}
