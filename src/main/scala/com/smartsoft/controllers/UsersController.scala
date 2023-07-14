package com.smartsoft.controllers

import com.smartsoft.model.{ErrorInfo, InternalServerError, User, UserSessionDetails}
import com.smartsoft.security.APISecurity
import com.smartsoft.services.UserService
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.ExecutionContext
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.{oneOf, oneOfVariant, query, statusCode}
import sttp.tapir._

class UsersController(userService: UserService, apiSecurity: APISecurity)(implicit ec: ExecutionContext) extends APIController{
  val registrationEndpoint = baseEndpoint
    .in("users")
    .description("Provides the functionality to allow user registration")
    .post
    .in(jsonBody[User])
    .errorOut(
      oneOf[ErrorInfo](
        // returns required http code for different types of ErrorInfo. For secured endpoint you need to define all cases before defining security logic
        oneOfVariant(statusCode(StatusCode.InternalServerError)
          .and(jsonBody[InternalServerError].description("An unexpected error has occurred"))
        )
      )
    )
    .out(jsonBody[User])
    .serverLogic(userService.signUp)

  val getSessionHistoryEndpoint = apiSecurity.withSecurity(baseEndpoint)
    .in("users")
    .in(path[String]("userCode"))
    .in("sessions")
    .description("Retrieves all session history for an specific user")
    .get
    .out(jsonBody[List[UserSessionDetails]])
    .serverLogic(_ => userService.getSessionHistory)


  val invalidateAllActiveSessionsEndpoint = apiSecurity.withSecurity(baseEndpoint)
    .in("users")
    .in(path[String]("userCode"))
    .in("sessions" / "state")
    .description("Invalidate all active sessions")
    .put
    .in(query[String]("action"))
    .serverLogic(_ => (userService.invalidateAllActiveSessions _).tupled )

  val allServerEndpoints = List(
    registrationEndpoint,
    getSessionHistoryEndpoint,
    invalidateAllActiveSessionsEndpoint
  )
}
