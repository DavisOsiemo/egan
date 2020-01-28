package com.africasTalking.egan.core
package db.mysql.mapper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.github.mauricio.async.db.RowData

import com.africasTalking._

import egan.core.db.mysql.EganMySqlDbService.UserDbEntry
import egan.core.db.mysql.service.EganMySqlDb

object UserMapper extends EganMySqlDb {
  private val FetchAllSql = "SELECT * FROM users"

  def fetchAll(): Future[List[UserDbEntry]] = {
    pool.sendPreparedStatement(FetchAllSql).map { queryResult =>
      queryResult.rows match {
        case Some(rows) => rows.toList map (x => rowToModel(x))
        case None => List()
      }
    }
  }
  private def rowToModel(row: RowData): UserDbEntry = {
    UserDbEntry (
      userId   = row("userId").asInstanceOf[Int],
      username = row("username").asInstanceOf[String],
      apiKey   = row("apiKey").asInstanceOf[String]
    )
  }
}
