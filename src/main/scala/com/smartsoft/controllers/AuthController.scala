package com.smartsoft.controllers

import com.smartsoft.model.AuthenticationResponse
import com.smartsoft.security.{APISecurity, AuthenticationService}
import com.smartsoft.services.CandidatesService
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.ExecutionContext
import sttp.tapir.generic.auto._
import io.circe.generic.auto._

class AuthController (apiSecurity: APISecurity, authenticationService: AuthenticationService)
                     (implicit ec: ExecutionContext) extends APIController {
  val authEndpoint = baseEndpoint
    .in("auth")
    .description("Provides the authentication mechanism")
    .post
    .out(jsonBody[AuthenticationResponse])
    .serverLogic(authenticationService.)




}
