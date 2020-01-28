package com.africasTalking.egan.web
package service

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.Props
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.util.Timeout

import io.atlabs._

import horus.core.config.ATConfig

import com.africasTalking._

import egan.core.util.EganCoreServiceT

import egan.handler.callback.FoodOrderCallbackService
import egan.handler.service.FoodOrderService

import egan.web.marshalling._

trait EganWebServiceT extends EganCoreServiceT with AuthenticationDirectiveT
  with WebJsonSupportT
  with HttpDirectivesT {

  override def snoopServiceName: String = "egan-web"

  def getSecurityHeaders = Set("apiKey")

  implicit val timeout = Timeout(ATConfig.httpRequestTimeout)

  private val foodOrderService         = actorRefFactory.actorOf(Props[FoodOrderService])
  private val foodOrderCallbackService = actorRefFactory.actorOf(Props[FoodOrderCallbackService])

  import FoodOrderService._

  lazy val route = {
    path("food" / "order" / "create") {
      logRequestResult("food:order:create", Logging.InfoLevel) {
        handleExceptions(exceptionHandler) {
          post {
            entity(as[FoodOrderRequest]) { request =>
              authenticateUser(request.username) {
                complete((foodOrderService ? request.getServiceRequest).mapTo[FoodOrderServiceResponse] map { x =>
                  FoodOrderResponse.getServiceResponse(x)
                })
              }
            }
          }
        }
      }
    } ~
    path ("food" / "order" / "callback") {
      logRequestResult("food:order:callback", Logging.InfoLevel){
        handleExceptions(exceptionHandler) {
          post {
            entity(as[FoodOrderCallbackRequest]) {request =>
              foodOrderCallbackService ! request.getCallbackServiceRequest
              complete(StatusCodes.OK)
            }
          }
        }
      }
    }
  }
}