package com.africasTalking.egan.handler
package service

import java.util.UUID
import java.util.Date

import scala.util.{ Failure, Success, Try }
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.util.Timeout

import spray.json._

import io.atlabs._

import horus.core.config.ATConfig
import horus.core.db.redis.RedisDbService._
import horus.core.http.client.ATHttpClientT
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import egan.core.util.EganEnum.{ FoodServiceStatus, FoodType, RequestStatus }
import egan.core.config.EganConfig
import egan.core.db.cassandra.service.BrokerRequestCassandraDbService
import egan.core.db.cassandra.service.BrokerRequestCassandraDbService._
import egan.core.db.redis.EganRedisDb

import egan.handler.service.FoodOrderMarshalling._
import egan.handler.util.HandlerJsonSupport._
import egan.handler.util.Message.FoodPendingStatusElement

object FoodOrderService {

  case class FoodOrderServiceRequest(
    name: FoodType.Value,
    quantity: Int,
    callbackUrl: String
  ) extends ATCCPrinter

  case class FoodOrderServiceResponse(
    transactionId: String,
    status: FoodServiceStatus.Value,
    description: String
  ) extends ATCCPrinter
}

class FoodOrderService extends Actor with ActorLogging with SnoopErrorPublisherT with ATHttpClientT
  with EtherGatewayJsonSupportT {

  override implicit val system: ActorSystem = context.system

  private val gatewayUrl            = EganConfig.brokerGatewayUrl.toString
  private val eganCallBackUrl       = EganConfig.eganCallBackUrl.toString
  private val CallbackCacheLifetime = EganConfig.cacheLifetime

  lazy val redisDbService            = createRedisDbService
  def createRedisDbService  = EganRedisDb.getInstance

  private lazy val brokerRequestCassandraDbService  = createBrokerRequestCassandraDbService
  def createBrokerRequestCassandraDbService         = context.actorOf(Props[BrokerRequestCassandraDbService])

  implicit val timeout               = Timeout(ATConfig.redisDbTimeout)

  import FoodOrderService._
  override def receive: Receive = {
    case req: FoodOrderServiceRequest =>
      log.info("processing {}", req)
      val currentSender = sender
      val transactionId = UUID.randomUUID.toString
      val userId        = UUID.randomUUID
      val brokerRequest = BrokerServiceGatewayRequest(
        name        = req.name,
        quantity    = req.quantity,
        callbackUrl = eganCallBackUrl
      )
      Marshal(brokerRequest).to[RequestEntity] flatMap { entity =>
        sendHttpRequest(HttpRequest(
          method  = HttpMethods.POST,
          uri     = gatewayUrl,
          headers = List(
            Accept(MediaRange(MediaTypes.`application/json`))
          ),
          entity  = entity
        ))
      } onComplete {
        case Failure(ex)  =>
          publishError("Error while processing gateway request: " + req, Some(ex))
          currentSender ! FoodOrderServiceResponse(
            transactionId = transactionId,
            status        = FoodServiceStatus.InternalError,
            description   = "Internal error occurred"
          )
        case Success(httpResponse) =>
          brokerRequestCassandraDbService ! BrokerRequestCreateDbQuery(
            userId          = userId,
            name            = req.name.toString,
            quantity        = req.quantity,
            callbackUrl     = req.callbackUrl,
            insertion_time  = new Date
          )
          httpResponse.status.isSuccess match {
            case false =>
              publishError(s"$transactionId: Unexpected response received: " + httpResponse)
              currentSender ! FoodOrderServiceResponse(
                transactionId = transactionId,
                status        = FoodServiceStatus.GatewayResponseError,
                description   = "Service currently unavailable"
              )
            case true  =>
              Try(httpResponse.data.parseJson.convertTo[BrokerServiceGatewayResponse]).toOption match{
                case None =>
                  publishError(s"$transactionId: No response data received: " + httpResponse)
                  currentSender ! FoodOrderServiceResponse(
                    transactionId = transactionId,
                    status        = FoodServiceStatus.GatewayResponseError,
                    description   = "Gateway Response error occurred"
                  )
                case Some(response) =>
                  RequestStatus.isSuccess(response.status) match {
                    case true  =>
                      currentSender ! FoodOrderServiceResponse(
                        transactionId = transactionId,
                        status        = FoodServiceStatus.Success,
                        description   = response.description
                      )
                      val providerId    = response.transactionId
                      val pendingStatus = FoodPendingStatusElement(
                        transactionId = transactionId,
                        providerId    = providerId,
                        callbackUrl   = req.callbackUrl
                      )
                      redisDbService ! AddElementQuery(
                        key      = providerId,
                        value    = pendingStatus.toJson.toString,
                        lifetime = Some(CallbackCacheLifetime)
                      )
                    case false =>
                      currentSender ! FoodOrderServiceResponse(
                        transactionId = transactionId,
                        status        = FoodServiceStatus.Failure,
                        description   = response.description
                      )
                  }
              }
          }
      }
  }
}