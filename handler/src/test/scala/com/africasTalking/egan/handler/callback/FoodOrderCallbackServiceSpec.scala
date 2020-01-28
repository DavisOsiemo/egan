package com.africasTalking.egan.handler
package callback

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.language.postfixOps
import scala.language.implicitConversions

import akka.actor.Props
import akka.http.scaladsl.model.{ HttpRequest, StatusCodes }
import akka.testkit.TestProbe

import spray.json._

import io.atlabs._

import horus.core.db.redis.RedisDbService._
import horus.core.http.client.ATHttpClientResponse

import com.africasTalking._

import egan.core.util.EganEnum.RequestStatus

import egan.handler.callback.FoodOrderCallbackService.FoodOrderCallbackServiceRequest
import egan.handler.util.Message.FoodPendingStatusElement
import egan.handler.callback.FoodOrderCallbackMarshalling._
import egan.handler.test.EganTestHttpEndpointT
import egan.handler.util.HandlerJsonSupport._

class FoodOrderCallbackServiceSpec extends EganTestHttpEndpointT with ClientGatewayJsonSupportT {

  val redisDbServiceProbe     = TestProbe()
  private val callbackService = system.actorOf(Props(new FoodOrderCallbackService {
    override def sendHttpRequest(req: HttpRequest): Future[ATHttpClientResponse] = getStringHttpResponse(req)
    override def createRedisDbService = redisDbServiceProbe.ref
  }))
  val transactionId = "sometransactionId"

  "The FoodOrderCallbackServiceSpec" must {
    "Fetch transactionId status from redis cache" in {
      val pendingRequest = FoodPendingStatusElement(
        transactionId = "987654345627893298",
        providerId    = transactionId,
        callbackUrl   = "http://www.paymenow.com"
      )
      callbackService ! FoodOrderCallbackServiceRequest(
        status        =  RequestStatus.Accepted,
        description   = "Success",
        transactionId = transactionId
      )
      val fetchQuery = redisDbServiceProbe.expectMsgType[FetchElementQuery]
      fetchQuery should be (FetchElementQuery(
        key = transactionId
      ))
      redisDbServiceProbe.reply(FetchElementResult(
        query = fetchQuery,
        value = Some(pendingRequest.toJson.toString)
      ))
      redisDbServiceProbe.expectMsg(
        DeleteElementQuery(
          key = transactionId
        )
      )
      expectNoMessage(1000.millis)
      redisDbServiceProbe.expectNoMessage(1000.millis)
    }
  }

  override def getStringHttpResponseImpl(data: String): ATHttpClientResponse = {
    ATHttpClientResponse(
      status = StatusCodes.OK,
      data   = "Success"
    )
  }

}