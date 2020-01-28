package com.africasTalking.egan.handler
package service

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import spray.json._

import io.atlabs._

import horus.core.util.ATCCPrinter

import com.africasTalking._

import egan.core.util.EganEnum._
import egan.core.util._

private[service] object FoodOrderMarshalling {

  case class BrokerServiceGatewayRequest(
    name: FoodType.Value,
    quantity: Int,
    callbackUrl: String
  ) extends ATCCPrinter

  case class BrokerServiceGatewayResponse(
    status: RequestStatus.Value,
    transactionId: String,
    description: String
  ) extends ATCCPrinter

  trait EtherGatewayJsonSupportT extends SprayJsonSupport
    with DefaultJsonProtocol {
    import EganJsonProtocol._

    implicit val BrokerServiceGatewayRequestFormat   = jsonFormat3(BrokerServiceGatewayRequest)
    implicit val BrokerServiceGatewayResponseFormat = jsonFormat3(BrokerServiceGatewayResponse)
  }

}
