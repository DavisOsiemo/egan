package com.africasTalking.egan.core
package db.cassandra.service

import scala.concurrent.ExecutionContext.Implicits.global

import java.util.UUID

import akka.actor.{ Actor, ActorLogging }
import akka.pattern.pipe

import com.datastax.driver.core.ResultSet

import io.atlabs._

import horus.core.db.cassandra.CassandraDbQueryResult
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import egan.core.db.cassandra.mapper.BrokerResponseMapper

object BrokerResponseCassandraDbService {

  case class BrokerResponseDbEntry(
     transactionId: String,
     status: String,
     description: String
  ) extends ATCCPrinter

  case class BrokerResponseCreateDbQuery(
     transactionId: String,
     status: String,
     description: String
  ) extends ATCCPrinter

  case class BrokerResponseFetchDbQuery(
    transactionId: String,
    start: Option[Int],
    limit: Int
  ) extends ATCCPrinter

}

class BrokerResponseCassandraDbService extends Actor
  with ActorLogging
  with SnoopErrorPublisherT
{

  import BrokerResponseCassandraDbService._

  def receive = {
    case x: BrokerResponseCreateDbQuery =>
      log.info("processing " + x)
      val currentSender = sender
      BrokerResponseMapper.insertNewRecord(
        transactionId = x.transactionId,
        status        = x.status,
        description   = x.description
      ).mapTo[List[ResultSet]] map { x => {
        currentSender ! ( x map (y =>  CassandraDbQueryResult(y)))
      }}

    case BrokerResponseFetchDbQuery(transactionId, start, limit) =>
      BrokerResponseMapper.fetchByTransactionId(transactionId, start, limit).mapTo[Iterator[BrokerResponseDbEntry]] pipeTo sender
  }

}
