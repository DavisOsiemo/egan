package com.africasTalking.egan.core
package util

object EganEnum {

  object FoodType extends Enumeration {
    val Ugali, Rice, BeefStew, BeefFry, Egusi, PepperSoup = Value
  }

  object RequestStatus extends Enumeration {
    val Accepted, Delivered, Failure = Value

    def isSuccess(value: Value): Boolean = value != Failure
  }

  object FoodServiceStatus extends Enumeration {
    val Success, Failure, InternalError, GatewayResponseError =  Value
  }
}
