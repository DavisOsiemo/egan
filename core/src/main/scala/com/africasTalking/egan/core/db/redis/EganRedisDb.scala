package com.africasTalking.egan.core
package db.redis

import io.atlabs._

import horus.core.db.redis.ATRedisDbT

import com.africasTalking._

import egan.core.config.EganConfig

object EganRedisDb extends ATRedisDbT {
  val host       = EganConfig.eganRedisDbHost
  val port       = EganConfig.eganRedisDbPort
  val numWorkers = EganConfig.eganRedisDbNumWorkers
}
