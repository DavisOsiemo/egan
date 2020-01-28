package com.africasTalking.egan.core
package db.mysql.service

import com.github.mauricio.async.db.Configuration
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ ConnectionPool, PoolConfiguration }

import com.africasTalking._

import egan.core.config.EganConfig

object EganMySqlDb {
  private val configuration = new Configuration(
    username = EganConfig.mysqlDbUser,
    host     = EganConfig.mysqlDbHost,
    port     = EganConfig.mysqlDbPort,
    password = Some(EganConfig.mysqlDbPass),
    database = Some(EganConfig.mysqlDbName)
  )

  private val poolConfiguration = new PoolConfiguration(
    maxIdle      = EganConfig.mysqlDbPoolMaxIdle,
    maxObjects   = EganConfig.mysqlDbPoolMaxObjects,
    maxQueueSize = EganConfig.mysqlDbPoolMaxQueueSize
  )

  private val factory = new MySQLConnectionFactory(configuration)
  private val pool    = new ConnectionPool(factory, poolConfiguration)
}

trait EganMySqlDb {
  implicit lazy val pool = EganMySqlDb.pool
}