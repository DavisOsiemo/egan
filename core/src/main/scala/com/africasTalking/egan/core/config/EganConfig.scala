package com.africasTalking.egan.core
package config

import scala.collection.JavaConverters._

import java.net.InetSocketAddress

import com.typesafe.config.ConfigFactory

import io.atlabs._

import horus.core.config.ATBaseConfigT
import horus.core.util.ATUtil

object EganConfig extends EganConfigT

private[core] trait EganConfigT extends ATBaseConfigT {
  config.checkValid(ConfigFactory.defaultReference)

  val webHost = config.getString("egan.interface.web.host")
  val webPort = config.getInt("egan.interface.web.port")

  val brokerGatewayUrl = ATUtil.parseUrl(config.getString("egan.broker.gateway-url")).get
  val eganCallBackUrl  = ATUtil.parseUrl(config.getString("egan.broker.callback-url")).get

  //DB: Mysql
  val mysqlDbUser             = config.getString("egan.db.mysql.user")
  val mysqlDbHost             = config.getString("egan.db.mysql.host")
  val mysqlDbPort             = config.getInt("egan.db.mysql.port")
  val mysqlDbPass             = config.getString("egan.db.mysql.pass")
  val mysqlDbName             = config.getString("egan.db.mysql.name")
  val mysqlDbPoolMaxIdle      = config.getInt("egan.db.mysql.pool.max-idle")
  val mysqlDbPoolMaxObjects   = config.getInt("egan.db.mysql.pool.max-objects")
  val mysqlDbPoolMaxQueueSize = config.getInt("egan.db.mysql.pool.max-queue-size")

  val mysqlDbUserCacheUpdateFrequency = ATUtil.parseFiniteDuration(config.getString("egan.db.mysql.cache-update-frequency.user")).get

  //DB: Redis
  val eganRedisDbHost         = config.getString("egan.db.redis.egan.host")
  val eganRedisDbPort         = config.getInt("egan.db.redis.egan.port")
  val eganRedisDbNumWorkers   = config.getInt("egan.db.redis.egan.num-workers")

  val cacheLifetime       = ATUtil.parseFiniteDuration(config.getString("egan.callback.cache-lifetime")).get
  val callbackUrlTimeout  = ATUtil.parseFiniteDuration(config.getString("egan.actor-timeout.callback-url")).get

  //DB: Cassandra
  val cassandraUsername   = config.getString("egan.db.cassandra.username")
  val cassandraPassword   = config.getString("egan.db.cassandra.password")
  val cassandraHosts      = config.getStringList("egan.db.cassandra.hosts").asScala.toList
  val cassandraPort       = config.getInt("egan.db.cassandra.port")
  val cassandraKeyspace   = config.getString("egan.db.cassandra.key-space")
  val cassandraAddressTranslationMap: Map[InetSocketAddress, InetSocketAddress] = {
    val element = config.getObject("egan.db.cassandra.address-translation-map").asScala
    element.foldLeft(Map[InetSocketAddress, InetSocketAddress]()) {
      case (m, elem) =>
        m.updated(
          getSocketAddress(elem._1),
          getSocketAddress(elem._2.unwrapped.asInstanceOf[String])
        )
    }
  }

  private def getSocketAddress(str: String) : InetSocketAddress = {
    val parts = str.split(":")
    assert(parts.length == 2, "Invalid socket address" + str)
    new InetSocketAddress(parts(0), parts(1).toInt)
  }

}
