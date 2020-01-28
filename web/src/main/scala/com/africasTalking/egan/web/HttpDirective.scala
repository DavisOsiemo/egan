package com.africasTalking.egan.web

import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.{ Directives, ExceptionHandler }

trait HttpDirectivesT extends Directives  {

  implicit def exceptionHandler =
    ExceptionHandler {
      case e: Throwable =>
        val exceptionMessage = Option(e.getMessage).getOrElse("Unexpected error")
        complete(BadRequest, exceptionMessage)
    }

}