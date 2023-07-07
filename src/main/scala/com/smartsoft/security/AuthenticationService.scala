package com.smartsoft.security

import com.smartsoft.model.{ErrorInfo, User}
import sttp.model.StatusCode

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class AuthenticationService (jwtService: JwtService)(implicit ec: ExecutionContext) {
  def authenticate(token: String): Future[Either[ErrorInfo, User]] = {
    Future(Right(User("","","","",Option(""),"",Option(LocalDateTime.now()))))
//      jwtService.getUserCode(token: String).map {
//        case Some(userCode) =>
//          Left(ErrorInfo(StatusCode.Unauthorized, "Token is expired/invalid"))
//        case Right(Some(user)) => Right(user)
//        case Right(None) => Left(ErrorInfo(StatusCode.Unauthorized, "Invalid user/password"))
//      }
  }

  def login(loginRequest: LoginRequest): Unit = {

  }
}
