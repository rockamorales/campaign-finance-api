package com.smartsoft.controllers

import com.smartsoft.model.{ErrorInfo, LoginResponse}
import com.smartsoft.security.{APISecurity, AuthenticationService, LoginRequest}
import com.smartsoft.services.{CandidatesService, UserService}
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.ExecutionContext
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.{oneOf, oneOfVariant, statusCode}

class AuthController (userService: UserService)
                     (implicit ec: ExecutionContext) extends APIController {
  val authEndpoint = baseEndpoint
    .in("auth")
    .description("Provides the authentication mechanism")
    .post
    .in(jsonBody[LoginRequest])
    .errorOut(
      oneOf[ErrorInfo](
        // returns required http code for different types of ErrorInfo. For secured endpoint you need to define all cases before defining security logic
        oneOfVariant(statusCode(StatusCode.Unauthorized)
          .and(jsonBody[ErrorInfo].description("When user is not authenticated or token is expired"))
        )
      )
    )
    .out(jsonBody[LoginResponse])
    .serverLogic(userService.login _)




}
