package com.africasTalking.egan.handler
package service

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.language.implicitConversions

import akka.actor._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.testkit.TestProbe

import spray.json._

import io.atlabs._

import horus.core.db.redis.RedisDbService.AddElementQuery
import horus.core.http.client._

import com.africasTalking._

import egan.core.util.EganEnum._
import egan.core.util._

import egan.handler.service.FoodOrderMarshalling._
import egan.handler.service.FoodOrderService.FoodOrderServiceRequest
import egan.handler.test.EganTestHttpEndpointT
import egan.handler.util.Message.FoodPendingStatusElement
import egan.handler.util.HandlerJsonSupport._

trait TestJsonSupportT extends SprayJsonSupport
  with DefaultJsonProtocol {
  import EganJsonProtocol._
  implicit val FoodOrderServiceRequestFormat = jsonFormat3(FoodOrderServiceRequest)
}

class FoodOrderServiceSpec extends EganTestHttpEndpointT
  with EtherGatewayJsonSupportT
  with TestJsonSupportT {

  val redisDbServiceProbe = TestProbe()
  private val service     = system.actorOf(Props(new FoodOrderService {
    override def sendHttpRequest(req: HttpRequest): Future[ATHttpClientResponse] = getStringHttpResponse(req)
    override def createRedisDbService = redisDbServiceProbe.ref
  }))

  private val oneWeekLifetime  = Some(FiniteDuration(7, DAYS))

  import FoodOrderService._
  "The FoodService" must {
    "process a 'bad' HTTP status code in the gateway response correctly" in {
      service ! FoodOrderServiceRequest(
        name        = FoodType.PepperSoup,
        quantity    = 1,
        callbackUrl = "http://somecallback.com"
      )
      val response = expectMsgType[FoodOrderServiceResponse]
      response.status shouldBe FoodServiceStatus.GatewayResponseError

      expectNoMessage(100.millis)
    }

    "process a 'good' HTTP status code but bad payload in the gateway response correctly" in {
      service ! FoodOrderServiceRequest(
        name        = FoodType.BeefFry,
        quantity    = 1,
        callbackUrl = "http://somecallback.com"
      )
      val response = expectMsgType[FoodOrderServiceResponse]
      response.status shouldBe FoodServiceStatus.GatewayResponseError

      expectNoMessage(100.millis)
    }

    "process a successful gateway response correctly" in {
      val callbackUrl =  "http://somecallback.com"
      service ! FoodOrderServiceRequest(
        name        = FoodType.Ugali,
        quantity    = 1,
        callbackUrl = callbackUrl
      )
      val response = expectMsgType[FoodOrderServiceResponse]
      response.status shouldBe FoodServiceStatus.Success
      val addElementQuery = redisDbServiceProbe.expectMsgType[AddElementQuery]
      addElementQuery should be (AddElementQuery(
        key      = addElementQuery.key,
        value    = addElementQuery.value,
        lifetime = oneWeekLifetime
      ))
      val payloadValue = addElementQuery.value.parseJson.convertTo[FoodPendingStatusElement]
      payloadValue should be (FoodPendingStatusElement(
        transactionId = payloadValue.transactionId,
        providerId    = payloadValue.providerId,
        callbackUrl   = callbackUrl,
      ))
      expectNoMessage(100.millis)
      redisDbServiceProbe.expectNoMessage(100 millis)
    }

    "process a failed gateway response correctly" in {
      service ! FoodOrderServiceRequest(
        name     = FoodType.Egusi,
        quantity = 1,
        callbackUrl = "http://brenda.com"
      )
      val response = expectMsgType[FoodOrderServiceResponse]
      response.status shouldBe FoodServiceStatus.Failure

      expectNoMessage(100.millis)
    }
  }

  override def getStringHttpResponseImpl(data: String): ATHttpClientResponse = {
    val request = data.parseJson.convertTo[FoodOrderServiceRequest]
    request.name match {
      case FoodType.Ugali => ATHttpClientResponse(
        status = StatusCodes.OK,
        data   = BrokerServiceGatewayResponse(
          status        = RequestStatus.Accepted,
          transactionId = "123abc",
          description   = "Accepted transaction"
        ).toJson.toString
      )
      case FoodType.Egusi => ATHttpClientResponse(
        status = StatusCodes.OK,
        data   = BrokerServiceGatewayResponse(
          status        = RequestStatus.Failure,
          transactionId = "123string",
          description   = "Failed transaction"
        ).toJson.toString
      )
      case FoodType.BeefFry => ATHttpClientResponse(
        status = StatusCodes.OK,
        data   = "This is a simple sentence."
      )
      case FoodType.PepperSoup => ATHttpClientResponse(
        status = StatusCodes.BadRequest,
        data   = "Bad request response."
      )
    }
  }
}
