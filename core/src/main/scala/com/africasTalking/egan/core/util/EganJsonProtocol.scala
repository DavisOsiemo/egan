package com.africasTalking.egan.core
package util

import spray.json.DefaultJsonProtocol

import io.atlabs._

import horus.core.util.ATUtil

import com.africasTalking._

import egan.core.util.EganEnum._

object EganJsonProtocol extends DefaultJsonProtocol {

  implicit val FoodTypeFormat           = ATUtil.enumJsonFormat(FoodType)
  implicit val RequestStatusFormat      = ATUtil.enumJsonFormat(RequestStatus)
  implicit val FoodServiceStatusFormat  = ATUtil.enumJsonFormat(FoodServiceStatus)

}
