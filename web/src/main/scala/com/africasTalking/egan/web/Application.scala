package com.africasTalking.egan.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import io.atlabs._

import horus.core.util.{ ATLogT, ApplicationLifecycle }

import com.africasTalking._

import egan.core.config.EganConfig

import egan.web.service.EganWebServiceT

class Application extends ApplicationLifecycle with ATLogT {

  private[this] var started: Boolean = false

  private val applicationName = "egan-web"

  implicit val actorSystem    = ActorSystem(s"$applicationName-system")

  def start() {
    log.info(s"Starting $applicationName Service")

    if (!started) {
      Http().bindAndHandle(
        new EganWebServiceT {
           override def actorRefFactory = actorSystem
        }.route,
        EganConfig.webHost,
        EganConfig.webPort
      )
      started = true
    }
  }

  def stop() {
    log.info(s"Stopping $applicationName Service")

    if (started) {
      started = false
      actorSystem.terminate()
    }
  }

}