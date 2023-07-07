package com.smartsoft.controllers

import com.smartsoft.model.{ErrorInfo, LoginResponse, User, UserSessionDetails}
import com.smartsoft.security.LoginRequest
import com.smartsoft.services.UserService
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.ExecutionContext
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._

import sttp.tapir.{oneOf, oneOfVariant, query, statusCode}

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

  val registrationEndpoint = baseEndpoint
    .in("registration")
    .description("Provides the functionality to allow user registration")
    .post
    .in(jsonBody[User])
    .errorOut(
      oneOf[ErrorInfo](
        // returns required http code for different types of ErrorInfo. For secured endpoint you need to define all cases before defining security logic
        oneOfVariant(statusCode(StatusCode.InternalServerError)
          .and(jsonBody[ErrorInfo].description("An unexpected error has occurred"))
        )
      )
    )
    .out(jsonBody[User])
    .serverLogic(userService.signUp _)

  val getSessionHistoryEndpoint = baseEndpoint
    .in("sessions")
    .description("Retrieves all session history for an specific user")
    .get
    .in(query[String]("userCode"))
    .errorOut(
      oneOf[ErrorInfo](
        // returns required http code for different types of ErrorInfo. For secured endpoint you need to define all cases before defining security logic
        oneOfVariant(statusCode(StatusCode.InternalServerError)
          .and(jsonBody[ErrorInfo].description("An unexpected error has occurred"))
        )
      )
    )
    .out(jsonBody[List[UserSessionDetails]])
    .serverLogic(userService.getSessionHistory _)


  val invalidateAllActiveSessionsEndpoint = baseEndpoint
    .in("sessions" / "state")
    .description("Invalidate all active sessions")
    .put
    .in(query[String]("userCode"))
//    .errorOut(
//      oneOf[ErrorInfo](
//        // returns required http code for different types of ErrorInfo. For secured endpoint you need to define all cases before defining security logic
//        oneOfVariant(statusCode(StatusCode.InternalServerError)
//          .and(jsonBody[ErrorInfo].description("An unexpected error has occurred"))
//        )
//      )
//    )
    .serverLogic(userService.invalidateAllActiveSessions _)


  val allServerEndpoints = List(
    authEndpoint,
    registrationEndpoint,
    getSessionHistoryEndpoint,
    invalidateAllActiveSessionsEndpoint
  )
}
