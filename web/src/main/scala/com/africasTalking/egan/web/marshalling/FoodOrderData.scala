package com.africasTalking.egan
package web.marshalling

import io.atlabs._

import horus.core.util.{ ATCCPrinter, ATUtil }

import com.africasTalking._

import egan.core.util.EganEnum._

import egan.handler.callback.FoodOrderCallbackService._
import egan.handler.service.FoodOrderService._

case class FoodOrderRequest(
  name: FoodType.Value,
  quantity: Int,
  username: String,
  callbackUrl: String
) extends ATCCPrinter {
  require(quantity > 0, "Quantity must be greater than 0")
  def getServiceRequest = {
    require(ATUtil.parseUrl(callbackUrl).isDefined, "Invalid Callback URL")
    FoodOrderServiceRequest(
      name        = name,
      quantity    = quantity,
      callbackUrl = callbackUrl
    )
  }
}

case class FoodOrderResponse(
  transactionId: String,
  status: FoodServiceStatus.Value
) extends ATCCPrinter

object FoodOrderResponse {
  def getServiceResponse(response: FoodOrderServiceResponse) = {
    FoodOrderResponse (
      transactionId = response.transactionId,
      status        = response.status
    )
  }
}

case class FoodOrderCallbackRequest(
 status: String,
 description: String,
 transactionId: String
) extends ATCCPrinter {
  def getCallbackServiceRequest = {
    FoodOrderCallbackServiceRequest(
      status          = RequestStatus.withName(status),
      transactionId   = transactionId,
      description     = description,

    )
  }
}