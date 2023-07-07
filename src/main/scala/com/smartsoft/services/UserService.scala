package com.smartsoft.services

import akka.actor.ActorRef
import akka.pattern.StatusReply.ErrorMessage
import akka.pattern.ask
import akka.util.Timeout
import com.smartsoft.actors.SessionsPersistentActor.SessionHistoryResponse
import com.smartsoft.actors.UserPersistentActor.UserUnauthorized
import com.smartsoft.actors.{SessionsPersistentActor, UserPersistentActor}
import com.smartsoft.model.{ErrorInfo, LoginResponse, User, UserSessionDetails}
import com.smartsoft.security.LoginRequest
import sttp.model.StatusCode

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

case class UserService(userPersistentActor: ActorRef)(implicit val ec: ExecutionContext) {
  implicit val timeout: Timeout = Timeout(5 seconds)
  def login(loginRequest: LoginRequest): Future[Either[ErrorInfo, LoginResponse]] = {
    (userPersistentActor ? UserPersistentActor.LoginUser(loginRequest))
      .map {
        case SessionsPersistentActor.UserAuthenticated(userSession) => Right(LoginResponse(userSession.token))
        case SessionsPersistentActor.UserAuthorized(_, token) => Right(LoginResponse(token))
        case UserPersistentActor.UserUnauthorized(message) => Left(ErrorInfo(StatusCode.Unauthorized, message))
      }
  }

  def signUp(user: User): Future[Either[ErrorInfo, User]] = {
    (userPersistentActor ? UserPersistentActor.CreateUser(user))
      .map {
        case UserPersistentActor.UserCreatedResponse(user) => Right(user)
        case UserPersistentActor.RegistrationFailed(exception) => Left(ErrorInfo(StatusCode.InternalServerError, exception.getMessage))
      }
      .recoverWith {
        case exception =>
          Future(Left(ErrorInfo(StatusCode.InternalServerError, exception.getMessage)))
      }
  }

  def getSessionHistory(userCode: String): Future[Either[ErrorInfo, List[UserSessionDetails]]] = {
    (userPersistentActor ? UserPersistentActor.GetSessionHistory(userCode))
      .map {
        case SessionHistoryResponse(sessions) =>
          val sessionHistory = sessions.map(session =>
                                  UserSessionDetails(session.sessionId,session.userCode,session.startDate,
                                                      Option(session.endDate), session.state, session.message))
          Right(sessionHistory)
      }
      .recoverWith {
        case exception => Future(Left(ErrorInfo(StatusCode.InternalServerError, exception.getMessage)))
      }
  }


  def invalidateAllActiveSessions(userCode: String): Future[Either[Unit, Unit]] = {
    userPersistentActor ! UserPersistentActor.InvalidateAllActiveSessions(userCode)
    Future.successful(Right())
  }

}
