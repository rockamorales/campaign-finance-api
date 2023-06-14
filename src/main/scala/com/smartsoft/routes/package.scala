package com.smartsoft

import com.smartsoft.model.ErrorInfo
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import io.circe.generic.auto._
import sttp.tapir.generic.auto._

package object routes {
  val baseEndpoint: PublicEndpoint[Unit, ErrorInfo, Unit, Any] =
      endpoint.in("api" / "v1.0")
        .description("Campaign finance API")
        .errorOut(jsonBody[ErrorInfo]
          .description("If an error happened while executing the request, this object will contain the error details"))

}
