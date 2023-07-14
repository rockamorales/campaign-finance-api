package com.smartsoft.security

import com.smartsoft.model.{BadRequest, ErrorInfo, Forbidden, InternalServerError, NotFound, Unauthorized, User}
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
          oneOfVariant(statusCode(StatusCode.Forbidden).and(jsonBody[Forbidden].description("When user doesn't have role for the endpoint"))),
          oneOfVariant(statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized].description("When user is not authenticated or token is expired"))),
          oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound].description("When something not found"))),
          oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest].description("Bad request"))),
          oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[InternalServerError].description("For exceptional cases"))),
          // default case below.
          oneOfDefaultVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[InternalServerError].description("Default result").example(InternalServerError("Test error message"))))
        )
      )
      .serverSecurityLogic(authService.authenticate)
  }
}
