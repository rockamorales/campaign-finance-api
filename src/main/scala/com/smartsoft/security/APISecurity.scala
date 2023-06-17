package com.smartsoft.security

import com.smartsoft.model.{ErrorInfo, User}
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{PublicEndpoint, auth, oneOf, oneOfDefaultVariant, oneOfVariant, statusCode}
import sttp.tapir.server.PartialServerEndpoint
import io.circe.generic.auto._
import sttp.tapir.generic.auto._


import scala.concurrent.{ExecutionContext, Future}

class APISecurity(authService: AuthenticationService)(implicit ec: ExecutionContext) {
  def withSecurity(baseEndpoint: PublicEndpoint[Unit, Unit, Unit, Any]): PartialServerEndpoint[String, User, Unit, ErrorInfo, Unit, Any, Future] = {
    baseEndpoint // base tapir endpoint
      .securityIn(auth.bearer[String]().description("Bearer token from Authorization header")) // defining security input
      .errorOut(
        oneOf[ErrorInfo](
          // returns required http code for different types of ErrorInfo. For secured endpoint you need to define all cases before defining security logic
          oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[ErrorInfo].description("When user doesn't have role for the endpoint"))),
          oneOfVariant(statusCode(StatusCode.Unauthorized).and(jsonBody[ErrorInfo].description("When user is not authenticated or token is expired"))),
          oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[ErrorInfo].description("When something not found"))),
          oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorInfo].description("Bad request"))),
          oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[ErrorInfo].description("For exceptional cases"))),
          // default case below.
          oneOfDefaultVariant(jsonBody[ErrorInfo].description("Default result").example(ErrorInfo(StatusCode.InternalServerError, "Test error message")))
        )
      )
      .serverSecurityLogic(authService.authenticate(_))
  }
}
