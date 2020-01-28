package com.africasTalking.egan.web
package service

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.http.scaladsl.server._
import akka.util.ByteString
import akka.actor._
import akka.http.scaladsl.model._

import StatusCodes._

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import spray.json._

import com.africasTalking._

import egan.core.util.EganEnum.FoodType

import egan.web.marshalling.{ FoodOrderRequest, WebJsonSupportT }

import egan.handler.service.FoodOrderService.FoodOrderServiceResponse

class EganWebServiceSpec extends AnyWordSpec
  with Matchers
  with ScalatestRouteTest
  with EganWebServiceT
  with WebJsonSupportT
{

  override def actorRefFactory: ActorRefFactory   = system
  implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout(FiniteDuration(10, "seconds"))

  val validApiKey   ="apitest9"
  val validUsername = "user9"
  val invalidApiKey = "invalidtest"

  "EganWebService" should {

    val foodServiceRequestJson = FoodOrderRequest(FoodType.Ugali, 1, validUsername, "http:8080").toJson.toString

    "Accept a valid WebService POST request" in {

      HttpRequest(
        method  = HttpMethods.POST,
        uri     = "/food/order/create",
        entity  = HttpEntity(ContentTypes.`application/json`, ByteString(foodServiceRequestJson))
      ) ~> addHeader("apiKey", validApiKey) ~> Route.seal(route) ~> check {
        status shouldEqual OK
        responseAs[String].parseJson.convertTo[FoodOrderServiceResponse]
      }
    }

    "Reject a valid POST request with an invalid API Key" in {
      HttpRequest(
        method  = HttpMethods.POST,
        uri     = "/food/order/create",
        entity  = HttpEntity(ContentTypes.`application/json`, ByteString(foodServiceRequestJson))
      ) ~> addHeader("apiKey", invalidApiKey) ~> Route.seal(route) ~> check {
        status shouldEqual Unauthorized
        responseAs[String] shouldEqual "The supplied authentication is invalid"
      }
    }

    // Unhandled requests
    "return a MethodNotAllowed error for PUT requests to the messaging path" in {
      Put("/food/order/create") ~> Route.seal(route) ~> check {
        status shouldEqual MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }
    }

    "leave requests to base path unhandled" in {
      Get() ~> route ~> check {
        handled shouldEqual false
      }
    }

    "leave requests to other paths unhandled" in {
      Get("/other") ~> route ~> check {
        handled shouldEqual false
      }
    }
  }
}