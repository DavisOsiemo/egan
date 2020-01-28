package com.africasTalking.egan.core
package db.cassandra.service

import scala.concurrent.duration._

import scala.language.postfixOps

import java.util.Date
import java.util.UUID

import akka.actor.Props

import io.atlabs._

import horus.core.db.cassandra._

import com.africasTalking._

import egan.core.db.test._

import BrokerRequestCassandraDbService._

class BrokerRequestCassandraDbServiceSpec extends EganCoreTestServiceT {
  val dbService= system.actorOf(Props[BrokerRequestCassandraDbService])

  val userId          = UUID.randomUUID
  val name            = "fufu"
  val quantity        = -254538555
  val callbackUrl     = "https://testcallback.com"
  val insertion_time  = new Date

  "BrokerRequestCassandraDbService" must {
    "insert records correctly to DB" in {
      dbService ! BrokerRequestCreateDbQuery(
        userId          = userId,
        name            = name,
        quantity        = quantity,
        callbackUrl     = callbackUrl,
        insertion_time  = insertion_time
      )
      val results = expectMsgClass(
        5 seconds,
        classOf[List[CassandraDbQueryResult]]
      )
      results.foreach( _.status should be (true))

      expectNoMessage(100.millis)
    }
    "fetch results when filtered by userId and Ordered By insertion_time" in {
      dbService ! BrokerRequestFetchDbQuery(
        userId  = UUID.fromString("ab15aaae-32d0-48ca-be49-07cbb9c151dd"),
        start   = Some(0),
        limit   = 1
      )

      val result = expectMsgClass(
        5 seconds,
        classOf[Iterator[BrokerRequestDbEntry]]
      )

      result.hasNext should be (true)

      val dbEntry = result.next

      dbEntry.name should be (name)
      dbEntry.quantity should be (quantity)
      dbEntry.userId should be (UUID.fromString("ab15aaae-32d0-48ca-be49-07cbb9c151dd"))

      result.hasNext should be (false)

      expectNoMessage(100.millis)
    }
  }
}
