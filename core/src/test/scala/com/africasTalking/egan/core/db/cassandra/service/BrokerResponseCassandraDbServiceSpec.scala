package com.africasTalking.egan.core
package db.cassandra.service

import scala.language.postfixOps

import scala.concurrent.duration._

import akka.actor.Props

import io.atlabs._

import horus.core.db.cassandra._

import com.africasTalking._

import egan.core.db.test._

import BrokerResponseCassandraDbService._

class BrokerResponseCassandraDbServiceSpec extends EganCoreTestServiceT {
  val dbService = system.actorOf(Props[BrokerResponseCassandraDbService])

  val transactionId = "8d43a1eb-4655-42be-8a0d-833312d2212b"
  val status        = "Delivered"
  val description   = "Order has been delivered. Dance in the rain"

  "BrokerResponseCassandraDbService" must {
    "insert records correctly to DB" in {
      dbService ! BrokerResponseCreateDbQuery(
        transactionId = transactionId,
        status        = status,
        description   = description
      )
      val result  = expectMsgClass(
        5 seconds,
        classOf[List[CassandraDbQueryResult]]
      )
      result.foreach( _.status should be (true))

      expectNoMessage(100.millis)
    }
  }

  "fetch results when filtered by transactionId" in {
    dbService ! BrokerResponseFetchDbQuery(
      transactionId = transactionId,
      start         = Some(0),
      limit         = 1
    )

    val result = expectMsgClass(
      5 seconds,
      classOf[Iterator[BrokerResponseDbEntry]]
    )

    result.hasNext should be (true)

    val dbEntry = result.next

    dbEntry.status should be (status)
    dbEntry.description should be (description)
    dbEntry.transactionId should be (transactionId)

    result.hasNext should be (false)
  }
}
