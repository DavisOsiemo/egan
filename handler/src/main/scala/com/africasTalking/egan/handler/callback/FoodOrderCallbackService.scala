package com.africasTalking.egan.handler
package callback

import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, MessageEntity }
import akka.pattern.ask
import akka.util.Timeout

import spray.json._

import io.atlabs._

import horus.core.config.ATConfig
import horus.core.db.redis.RedisDbService._
import horus.core.http.client.ATHttpClientT
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import egan.core.db.cassandra.service.BrokerResponseCassandraDbService
import egan.core.db.cassandra.service.BrokerResponseCassandraDbService._
import egan.core.db.redis.EganRedisDb
import egan.core.util.EganEnum.RequestStatus

import egan.handler.callback.FoodOrderCallbackMarshalling._
import egan.handler.util.Message.FoodPendingStatusElement
import egan.handler.util.HandlerJsonSupport._

object FoodOrderCallbackService {

  case class FoodOrderCallbackServiceRequest(
    status: RequestStatus.Value,
    description: String,
    transactionId: String
  ) extends ATCCPrinter
}

class FoodOrderCallbackService extends Actor with ActorLogging
  with ATHttpClientT
  with SnoopErrorPublisherT
  with ClientGatewayJsonSupportT {

  override implicit val system = context.system
  implicit val timeout         = Timeout(ATConfig.redisDbTimeout)

  private lazy val redisDbService = createRedisDbService
  def createRedisDbService = EganRedisDb.getInstance

  private lazy val brokerResponseCassandraDbService = createBrokerResponseCassandraDbService
  def createBrokerResponseCassandraDbService  = context.actorOf(Props[BrokerResponseCassandraDbService])

  import FoodOrderCallbackService._
  override def receive: Receive = {
    case req: FoodOrderCallbackServiceRequest =>
      log.info("processing {}", req)
      val redisKey        = req.transactionId
      val fetchElementFut = (redisDbService ? FetchElementQuery(redisKey)).mapTo[FetchElementResult]
      fetchElementFut.onComplete {
      case Failure(error) =>
        publishError(s"Error while fetching $redisKey from redis for $req", Some(error))
      case Success(result) =>
        result.value match {
          case None =>
            publishError(s"Could not find the element in Redis for $redisKey and request $req")
          case Some(payload) =>
            val pendingElement = payload.parseJson.convertTo[FoodPendingStatusElement]
            val request        = ClientCallBackRequest(
              transactionId = pendingElement.transactionId,
              status        = req.status,
              description   = req.description
            )
            val sendFut = for {
              entity   <- Marshal(request).to[MessageEntity]
              response <- sendHttpRequest(
                HttpRequest(
                  method = HttpMethods.POST,
                  uri    = pendingElement.callbackUrl,
                  entity = entity
                ).withHeaders(
                  RawHeader("Accept", "application/json")
                ),
              )
            } yield response
            sendFut onComplete {
              case Failure(error) =>
                publishError("Error processing client callback request" + req, Some(error))
              case Success(_)     =>
                redisDbService ! DeleteElementQuery(redisKey)

                brokerResponseCassandraDbService ! BrokerResponseCreateDbQuery(
                  transactionId = pendingElement.transactionId,
                  status        = req.status.toString,
                  description   = req.description
                )
            }
        }
    }
  }
}