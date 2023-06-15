package com.smartsoft.controllers

import com.smartsoft.model.ErrorInfo
import com.smartsoft.security.APISecurity
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.generic.auto._


trait APIController {
  lazy val baseEndpoint: PublicEndpoint[Unit, ErrorInfo, Unit, Any] =
    endpoint.in("api" / "v1.0")
      .description("Campaign finance API")
      .errorOut(
        oneOf[ErrorInfo](
          oneOfVariant(
            statusCode(StatusCode.Forbidden)
              .and(jsonBody[ErrorInfo]
                .description("If an error happens while executing the request, this object will contain the error details")
          ))))

}
