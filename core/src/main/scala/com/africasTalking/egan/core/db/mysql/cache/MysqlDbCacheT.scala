package com.africasTalking.egan.core
package db.mysql.cache

import akka.actor.Props

import io.atlabs._

import horus.core.db.mysql.cache.{ MysqlDbCacheEntryT, MysqlDbCacheT }

import com.africasTalking._

import egan.core.db.mysql.EganMySqlDbService

trait EganMysqlDbCacheT[EntryT <: MysqlDbCacheEntryT] extends MysqlDbCacheT[EntryT] {

  override def createMysqlDbService = context.actorOf(Props[EganMySqlDbService])
}