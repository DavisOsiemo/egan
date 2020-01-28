package com.africasTalking.egan.core
package db.cassandra.service

import scala.concurrent.ExecutionContext.Implicits.global

import java.util.Date
import java.util.UUID

import akka.actor.{ Actor, ActorLogging }
import akka.pattern.pipe

import com.datastax.driver.core.ResultSet

import io.atlabs._

import horus.core.db.cassandra.CassandraDbQueryResult
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import egan.core.db.cassandra.mapper.BrokerRequestMapper

object BrokerRequestCassandraDbService {

  case class BrokerRequestDbEntry(
     userId: UUID,
     name: String,
     quantity: Int,
     callbackUrl: String,
     insertion_time: Date
  ) extends ATCCPrinter

  case class BrokerRequestCreateDbQuery(
     userId: UUID,
     name: String,
     quantity: Int,
     callbackUrl: String,
     insertion_time: Date
  ) extends ATCCPrinter

  case class BrokerRequestFetchDbQuery(
     userId: UUID,
     start: Option[Int],
     limit: Int
  ) extends ATCCPrinter

}

class BrokerRequestCassandraDbService extends Actor
  with ActorLogging
  with SnoopErrorPublisherT
{

  import BrokerRequestCassandraDbService._

  def receive = {
    case x: BrokerRequestCreateDbQuery =>
      log.info("processing " + x)
      val currentSender = sender
      BrokerRequestMapper.insertNewRecord(
        userId          = x.userId,
        name            = x.name,
        quantity        = x.quantity,
        callbackUrl     = x.callbackUrl,
        insertion_time  = x.insertion_time
      ).mapTo[List[ResultSet]] map { x => {
        currentSender ! (x map (y => CassandraDbQueryResult(y)))
      }}

    case BrokerRequestFetchDbQuery(userId, start, limit) =>
      BrokerRequestMapper.fetchByUserId(userId, start, limit).mapTo[Iterator[BrokerRequestDbEntry]] pipeTo sender
  }

}
