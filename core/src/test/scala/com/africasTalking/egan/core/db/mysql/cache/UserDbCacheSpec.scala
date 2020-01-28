package com.africasTalking.egan.core
package db.mysql.cache

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.Props
import akka.testkit.TestProbe

import com.africasTalking._

import egan.core.db.test.EganCoreTestServiceT
import egan.core.db.mysql.EganMySqlDbService.{ UserDbEntry, UserFetchDbQuery }

object TestUserDbCache extends UserDbCacheT

class UserDbCacheSpec extends EganCoreTestServiceT {

  val testUser1: UserDbEntry = UserDbEntry(
    userId = 1,
    username = "Username1",
    apiKey = "apikey123"
  )

  val mysqlProbe = TestProbe()

  "UserDbCache" must {
    "fetch entries from the database on initialization" in {
      TestUserDbCache.initialize(
        system.actorOf(Props(new UserDbCache(TestUserDbCache){
          override def createMysqlDbService = mysqlProbe.ref
        }))
      )
      mysqlProbe.expectMsg(
        5 seconds,
        UserFetchDbQuery
      )
      mysqlProbe.reply(
        List[UserDbEntry](
          testUser1
        )
      )
      expectNoMessage(2 seconds)
    }
  }
}