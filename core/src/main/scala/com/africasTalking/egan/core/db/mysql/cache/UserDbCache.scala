package com.africasTalking.egan.core
package db.mysql.cache

import scala.concurrent.ExecutionContext.Implicits.global

import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import akka.actor.Props

import io.atlabs._

import horus.core.config.ATConfig
import horus.core.db.mysql.cache.{ MysqlDbCacheManagerT, UpdateCacheRequestImpl }
import horus.core.util.{ ATCCPrinter, ATLogT, ATUtil }

import com.africasTalking._

import egan.core.db.mysql.EganMySqlDbService.{ UserDbEntry, UserFetchDbQuery }
import egan.core.config.EganConfig
import egan.core.db.mysql.cache.UserDbCache.AuthKey

object UserDbCache extends UserDbCacheT {
  private[cache] case class AuthKey(
     username: String,
     apiKey: String
   ) extends ATCCPrinter
}

trait UserDbCacheT extends MysqlDbCacheManagerT[UserDbEntry]
  with ATLogT {

  def authenticate(
    username: String,
    apiKey: String
  ): Option[UserDbEntry] = authenticationMap.get(AuthKey(
    username = username.toLowerCase,
    apiKey   = ATUtil.sha256Hash(apiKey)
  ))

  def props = Props(classOf[UserDbCache], this)

  override def setEntries(x: List[UserDbEntry]) {
    setAuthenticationMap(
      map = (x.foldLeft(Map[AuthKey, UserDbEntry]()) {
        case (m, entry) =>
          m.updated(
            key   = AuthKey(
              username = entry.username.toLowerCase,
              apiKey   = entry.apiKey
            ),
            value = entry
          )
      })
    )
  }

  private var authenticationMap = Map[AuthKey, UserDbEntry]()
  private def setAuthenticationMap(map: Map[AuthKey, UserDbEntry]) {
    authenticationMap = map
  }
}

class UserDbCache(
 val manager: UserDbCacheT
)  extends EganMysqlDbCacheT[UserDbEntry] {

  implicit val timeout         = Timeout(ATConfig.mysqlDbTimeout)
  override val updateFrequency = EganConfig.mysqlDbUserCacheUpdateFrequency

  override def specificReceive = {
    case UpdateCacheRequestImpl =>
      (mysqlDbService ? UserFetchDbQuery).mapTo[List[UserDbEntry]] pipeTo sender
  }
}