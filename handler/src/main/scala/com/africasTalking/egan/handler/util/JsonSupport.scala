package com.africasTalking.egan.handler
package util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import spray.json._

import Message._

object HandlerJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val pendingStatusElementFormat      = jsonFormat3(FoodPendingStatusElement)

}