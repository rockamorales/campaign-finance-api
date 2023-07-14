package com.smartsoft.services

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.smartsoft.actors.SessionsPersistentActor.SessionHistoryResponse
import com.smartsoft.actors.{UserPersistentActor, UsersSupervisorActor}
import com.smartsoft.model.{ErrorInfo, InternalServerError, User, UserSessionDetails}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

case class UserService(usersSupervisorActor: ActorRef)(implicit val ec: ExecutionContext){
  implicit val timeout: Timeout = Timeout(30 seconds)

  def signUp(user: User): Future[Either[ErrorInfo, User]] = {
    (usersSupervisorActor ? UsersSupervisorActor.CreateUser(user))
      .map {
        case UserPersistentActor.UserCreatedResponse(user) => Right(user)
        case UserPersistentActor.RegistrationFailed(exception) => Left(InternalServerError(exception.getMessage))
      }
      .recoverWith {
        case exception =>
          Future(Left(InternalServerError(exception.getMessage)))
      }
  }

  def getSessionHistory(userCode: String): Future[Either[ErrorInfo, List[UserSessionDetails]]] = {
    (usersSupervisorActor ? UsersSupervisorActor.GetSessionHistory(userCode))
      .map {
        case SessionHistoryResponse(sessions) =>
          val sessionHistory = sessions.map(session =>
                                  UserSessionDetails(session.sessionId,session.userCode,session.startDate,
                                                      Option(session.endDate), session.state, session.message))
          Right(sessionHistory)
      }
      .recoverWith {
        case exception => Future(Left(InternalServerError(exception.getMessage)))
      }
  }

  def invalidateAllActiveSessions(userCode: String, action: String): Future[Either[ErrorInfo, Unit]] = {
    action match {
      case "invalidate-all" =>
        usersSupervisorActor ! UsersSupervisorActor.InvalidateAllActiveSessions(userCode)
        Future.successful(Right())
      case _ => Future.successful(
        Left(InternalServerError(s"Action $action not supported"))
      )
    }
  }

}
