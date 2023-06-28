package com.smartsoft.controllers

import com.smartsoft.model.{ErrorInfo, User}
import com.smartsoft.security.AuthenticationService
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.server.PartialServerEndpoint

import scala.concurrent.{ExecutionContext, Future}


trait APIController {

  lazy val baseEndpoint: PublicEndpoint[Unit, Unit, Unit, Any] =
    endpoint.in("campaign-finance" / "api" / "v1_0")
      .description("Campaign finance API Version 1.0")
}
