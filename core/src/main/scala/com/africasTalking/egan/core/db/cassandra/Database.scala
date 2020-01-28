package com.africasTalking.egan.core
package db.cassandra

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl._

import mapper._

private[cassandra] object EganCassandraDb extends EganCassandraDb(EganCassandraConnector.connector)

private[cassandra] sealed class EganCassandraDb(override val connector: CassandraConnection) extends Database[EganCassandraDb](connector) {


  object IncomingBrokerRequestMapper extends IncomingBrokerRequestMapper with connector.Connector
  object IncomingBrokerResponseMapper extends IncomingBrokerResponseMapper with connector.Connector

}
