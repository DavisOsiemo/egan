package com.africasTalking.egan.core
package db.cassandra.mapper

import scala.concurrent.Future

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.dsl.context

import io.atlabs._

import horus.core.db.cassandra.ATCassandraTableT
import horus.core.snoop.ATSnoopErrorPublisherT

import com.africasTalking._

import egan.core.db.cassandra.EganCassandraDb
import egan.core.db.cassandra.service.BrokerResponseCassandraDbService.BrokerResponseDbEntry

private[cassandra] object BrokerResponseMapper {

  private val responseMapper = EganCassandraDb.IncomingBrokerResponseMapper

  def insertNewRecord(
    transactionId: String,
    status: String,
    description: String
  ) : Future[List[ResultSet]] =
    Future.sequence(List(responseMapper) map ( x => {
      x.insertNewRecord(
        transactionId = transactionId,
        status        = status,
        description   = description
      )
    }))

  def fetchByTransactionId(
    transactionId: String,
    start: Option[Int],
    limit: Int
  ) : Future[Iterator[BrokerResponseDbEntry]] = 
    responseMapper.fetchByTransactionId(
      transactionId = transactionId,
      start         = start,
      limit         = limit
    )
  
}

private[mapper] sealed trait BrokerResponseMapperT extends ATCassandraTableT [BrokerResponseMapper, BrokerResponseDbEntry] {

  object transaction_id extends StringColumn with PrimaryKey
  object status extends StringColumn
  object description extends StringColumn

}

private[mapper] sealed abstract class BrokerResponseMapper extends BrokerResponseMapperT with ATSnoopErrorPublisherT {

  def insertNewRecord(
    transactionId: String,
    status: String,
    description: String
  ) : Future[ResultSet] = {
    insertRecordImpl(
      insert
        .value(_.transaction_id, transactionId)
        .value(_.status, status)
        .value(_.description, description)
    )
  }

  def fetchByTransactionId(
    transactionId: String,
    start: Option[Int],
    limit: Int
 ) : Future[Iterator[BrokerResponseDbEntry]] = 
   fetchImpl(
     query = select
         .where(_.transaction_id eqs transactionId),
     start = start,
     limit = limit
   )

}

private[cassandra] abstract class IncomingBrokerResponseMapper extends BrokerResponseMapper {
  override lazy val tableName = "incoming_broker_response"
}
