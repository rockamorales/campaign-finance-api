package com.smartsoft.security

import com.smartsoft.model.{ErrorInfo, User}
import sttp.model.StatusCode

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationService (jwtService: JwtService)(implicit ec: ExecutionContext) {
  def authenticate(token: String): Future[Either[ErrorInfo, User]] = {
      jwtService.getUser(token: String).map {
        case Left(exception) => Left(ErrorInfo(StatusCode.Unauthorized, "Token is expired/invalid"))
        case Right(Some(user)) => Right(user)
        case Right(None) => Left(ErrorInfo(StatusCode.Unauthorized, "Invalid user/password"))
      }
  }
}
