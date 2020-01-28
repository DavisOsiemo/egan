package com.africasTalking.egan.core
package db.cassandra.mapper

import scala.concurrent.Future

import java.util.Date

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.dsl.context

import io.atlabs._

import horus.core.db.cassandra.ATCassandraTableT
import horus.core.snoop.ATSnoopErrorPublisherT

import com.africasTalking._

import egan.core.db.cassandra.EganCassandraDb
import egan.core.db.cassandra.service.BrokerRequestCassandraDbService.BrokerRequestDbEntry

private[cassandra] object BrokerRequestMapper {

  private val requestMapper = EganCassandraDb.IncomingBrokerRequestMapper

  def insertNewRecord(
    userId: UUID,
    name: String,
    quantity: Int,
    callbackUrl: String,
    insertion_time: Date
  ) : Future[List[ResultSet]] =
    Future.sequence(List(requestMapper) map ( x => {
      x.insertNewRecord(
        userId          = userId,
        name            = name,
        quantity        = quantity,
        callbackUrl     = callbackUrl,
        insertion_time  = insertion_time
      )
    }))

  def fetchByUserId(
    userId: UUID,
    start: Option[Int],
    limit: Int
  ) : Future[Iterator[BrokerRequestDbEntry]] =
    requestMapper.fetchByUserId(
      userId  = userId,
      start   = start,
      limit   = limit
    )

}

private[mapper] sealed trait BrokerRequestMapperT extends ATCassandraTableT[BrokerRequestMapper, BrokerRequestDbEntry] {

  object user_id extends UUIDColumn with PrimaryKey
  object name extends StringColumn
  object quantity extends IntColumn
  object callbackUrl extends StringColumn
  object insertion_time extends DateColumn with PrimaryKey

}

private[mapper] sealed abstract class BrokerRequestMapper extends BrokerRequestMapperT with ATSnoopErrorPublisherT {

  def insertNewRecord(
    userId: UUID,
    name: String,
    quantity: Int,
    callbackUrl: String,
    insertion_time: Date
  ): Future[ResultSet] = {
    insertRecordImpl(
      insert
        .value(_.user_id, userId)
        .value(_.name, name)
        .value(_.quantity, quantity)
        .value(_.callbackUrl, callbackUrl)
        .value(_.insertion_time, insertion_time)
    )
  }

  def fetchByUserId(
    userId: UUID,
    start: Option[Int],
    limit: Int
  ): Future[Iterator[BrokerRequestDbEntry]] =
    fetchImpl(
      query = select
          .where(_.user_id eqs userId).orderBy(_.insertion_time descending),
      start = start,
      limit = limit
    )

}

private[cassandra] abstract class IncomingBrokerRequestMapper extends BrokerRequestMapper {
  override lazy val tableName = "incoming_broker_request"
}
