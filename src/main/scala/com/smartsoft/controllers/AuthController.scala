package com.smartsoft.controllers

import com.smartsoft.model.{ErrorInfo, LoginResponse, Unauthorized}
import com.smartsoft.security.{AuthenticationService, LoginRequest}
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.ExecutionContext
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.{oneOf, oneOfVariant, statusCode}

class AuthController (authService: AuthenticationService)
                     (implicit ec: ExecutionContext) extends APIController {
  val authEndpoint = baseEndpoint
    .in("users" / "authentication")
    .description("Provides the authentication mechanism")
    .post
    .in(jsonBody[LoginRequest])
    .errorOut(
      oneOf[ErrorInfo](
        // returns required http code for different types of ErrorInfo. For secured endpoint you need to define all cases before defining security logic
        oneOfVariant(statusCode(StatusCode.Unauthorized)
          .and(jsonBody[Unauthorized].description("When user is not authenticated or token is expired"))
        )
      )
    )
    .out(jsonBody[LoginResponse])
    .serverLogic(authService.login _)

  val allServerEndpoints = List(
    authEndpoint
  )
}
