package com.africasTalking.egan.web

import akka.http.scaladsl.model.headers.HttpChallenges
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsRejected
import akka.http.scaladsl.server.{ AuthenticationFailedRejection, Directive0, Directives }

import com.africasTalking._

import egan.core.db.mysql.cache.{ UserDbCache, UserDbCacheT }

trait AuthenticationDirectiveT extends Directives {

  private val challenge         = HttpChallenges.basic("api")
  def userDbCache: UserDbCacheT = UserDbCache

  def authenticateUser(username: String): Directive0 = {
    headerValueByName("apiKey").flatMap { apiKey =>
      userDbCache.authenticate(
        username = username,
        apiKey   = apiKey
      ) match {
        case Some(x) => pass
        case None    => reject(AuthenticationFailedRejection(CredentialsRejected, challenge)): Directive0
      }
    }
  }
}
