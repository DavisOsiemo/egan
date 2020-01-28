package com.africasTalking.egan.handler
package test

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import io.atlabs._

import horus.core.http.client.ATHttpClientResponse

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal

trait EganTestHttpEndpointT extends EganHandlerServiceTest {

  def getStringHttpResponse(req: HttpRequest): Future[ATHttpClientResponse] = Future.successful {
    getStringHttpResponseImpl(Await.result(
      Unmarshal(req.entity).to[String],
      1.second
    ))
  }
  def getStringHttpResponseImpl(data: String): ATHttpClientResponse
}
