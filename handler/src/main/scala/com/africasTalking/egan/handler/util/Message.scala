package com.africasTalking.egan.handler
package util

import io.atlabs._

import horus.core.util.ATCCPrinter

object Message {

  case class FoodPendingStatusElement(
     transactionId: String,
     providerId: String,
     callbackUrl: String,
   ) extends ATCCPrinter

}




