package com.africasTalking.egan.handler
package callback

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import spray.json._

import io.atlabs._

import horus.core.util.ATCCPrinter

import com.africasTalking._

import egan.core.util.EganEnum.RequestStatus
import egan.core.util.EganJsonProtocol

private[callback] object FoodOrderCallbackMarshalling {

  case class ClientCallBackRequest(
    transactionId: String,
    status: RequestStatus.Value,
    description: String
  ) extends ATCCPrinter

  trait ClientGatewayJsonSupportT extends SprayJsonSupport
    with DefaultJsonProtocol {
    import EganJsonProtocol._

    implicit val ClientCallBackRequestFormat = jsonFormat3(ClientCallBackRequest)
  }

}
