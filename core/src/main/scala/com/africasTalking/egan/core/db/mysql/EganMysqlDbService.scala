package com.africasTalking.egan.core
package db.mysql

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.{ Actor, ActorLogging }
import akka.pattern.pipe

import io.atlabs._

import horus.core.db.mysql.cache.MysqlDbCacheEntryT
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import egan.core.db.mysql.mapper.UserMapper
import egan.core.db.mysql.EganMySqlDbService.{ UserDbEntry, UserFetchDbQuery }

object EganMySqlDbService {
  case object UserFetchDbQuery

  case class UserDbEntry (
    userId: Int,
    username: String,
    apiKey : String
  ) extends ATCCPrinter with MysqlDbCacheEntryT

  case class UserFindDbQuery(
    userId: Option[Int],
    username:Option[String]
  ) extends ATCCPrinter

}
class EganMySqlDbService extends Actor with ActorLogging with SnoopErrorPublisherT{
  def receive = {
    case UserFetchDbQuery =>
      UserMapper.fetchAll.mapTo[List[UserDbEntry]] pipeTo sender
  }
}