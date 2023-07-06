package com.smartsoft.services

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.smartsoft.actors.UserPersistentActor.UserUnauthorized
import com.smartsoft.actors.{SessionsPersistentActor, UserPersistentActor}
import com.smartsoft.model.{ErrorInfo, LoginResponse}
import com.smartsoft.security.LoginRequest
import sttp.model.StatusCode

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

case class UserService(userAuthManagement: ActorRef)(implicit val ec: ExecutionContext) {
  implicit val timeout: Timeout = Timeout(5 seconds)
  def login(loginRequest: LoginRequest): Future[Either[ErrorInfo, LoginResponse]] = {
    (userAuthManagement ? UserPersistentActor.LoginUser(loginRequest))
      .map {
        case SessionsPersistentActor.UserAuthorized(_, _, _, token) => Right(LoginResponse(token))
        case UserUnauthorized(message) => Left(ErrorInfo(StatusCode.Unauthorized, message))
      }
  }
}
