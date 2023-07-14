package com.smartsoft.security

import akka.actor.ActorRef
import com.smartsoft.actors.{SessionsPersistentActor, UserPersistentActor, UsersSupervisorActor}
import com.smartsoft.model.{ErrorInfo, LoginResponse, Unauthorized, User}

import scala.concurrent.{ExecutionContext, Future}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

class AuthenticationService (jwtService: JwtService, usersSupervisorActor: ActorRef)
                            (implicit ec: ExecutionContext) {
  implicit val timeout: Timeout = Timeout(30 seconds)

  def login(loginRequest: LoginRequest): Future[Either[ErrorInfo, LoginResponse]] = {
    (usersSupervisorActor ? UsersSupervisorActor.LoginUser(loginRequest))
      .map {
        case SessionsPersistentActor.UserAuthenticated(userSession) =>
          Right(LoginResponse(userSession.token))
        case UserPersistentActor.UserUnauthorized(message) =>
          Left(Unauthorized(message))
      }
  }

  def authenticate(token: String): Future[Either[ErrorInfo, User]] = {
    (usersSupervisorActor ? UsersSupervisorActor.Authenticate(token))
      .map {
        case UserPersistentActor.UserUnauthorized(message) =>
          Left(Unauthorized(message))
        case SessionsPersistentActor.UserUnauthorized(message) =>
          Left(Unauthorized(message))
        case SessionsPersistentActor.UserAuthorized(user) =>
          Right(user)
        case UsersSupervisorActor.UserUnauthorized(message) =>
          Left(Unauthorized(message))
      }
  }
}
