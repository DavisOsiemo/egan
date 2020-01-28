package com.africasTalking.egan.core
package db.cassandra

import io.atlabs._

import horus.core.db.cassandra.ATCassandraDbConnector
import horus.core.db.cassandra.ATCassandraDbConnector.{ AuthParams, ConnectorParams }

import com.africasTalking._

import egan.core.config.EganConfig

private[cassandra] object EganCassandraConnector extends ATCassandraDbConnector (
  params = ConnectorParams(
    port                  = EganConfig.cassandraPort,
    hosts                 = EganConfig.cassandraHosts,
    keySpace              = EganConfig.cassandraKeyspace,
    addressTranslationMap = EganConfig.cassandraAddressTranslationMap,
    auth                  = Some(AuthParams(
      username  = EganConfig.cassandraUsername,
      password  = EganConfig.cassandraPassword
    ))
  )
)
