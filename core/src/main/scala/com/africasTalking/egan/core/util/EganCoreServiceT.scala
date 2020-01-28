package com.africasTalking.egan.core
package util

import io.atlabs._

import horus.core.util.HorusCoreServiceT

import com.africasTalking._

import egan.core.db.redis.EganRedisDb
import egan.core.db.mysql.cache.UserDbCache

trait EganCoreServiceT extends HorusCoreServiceT {

  UserDbCache.initialize(actorRefFactory.actorOf(
    UserDbCache.props))

  EganRedisDb.initialize(actorRefFactory)
}
