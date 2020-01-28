package com.africasTalking.egan
package web.marshalling

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import spray.json.DefaultJsonProtocol

import com.africasTalking._

import egan.handler.service.FoodOrderService.FoodOrderServiceResponse

import egan.core.util._

trait WebJsonSupportT extends DefaultJsonProtocol with SprayJsonSupport {

  import EganJsonProtocol._

  implicit val FoodOrderRequestFormat          = jsonFormat4(FoodOrderRequest)
  implicit val FoodOrderResponseFormat         = jsonFormat2(FoodOrderResponse.apply)
  implicit val FoodOrderCallbackRequestFormat  = jsonFormat3(FoodOrderCallbackRequest)
  implicit val FoodOrderServiceResponseFormat  = jsonFormat3(FoodOrderServiceResponse)
}
