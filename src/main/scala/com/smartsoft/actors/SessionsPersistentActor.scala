package com.smartsoft.actors

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, ReplyToStrategy}
import com.smartsoft.security.LoginRequest

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

object SessionsPersistentActor {
  def props(userCode: String): Props = {
    Props(new SessionsPersistentActor(userCode))
  }

  //Commands
  case class LoginUser(loginRequest: LoginRequest, replyTo: ActorRef)
  case class InvalidateToken(token: String)
  case class Authenticate(token: String)

  //Events
  case class UserAuthorized(sessionId: String, userCode: String, created: LocalDateTime, token: String)
  case class Authenticated(sessionId: String, token: String)


  //Query
  object GetActiveSessions
  case class GetSessionDetails(sessionId: String)
  case class GetPaginatedSessionHistory(page: String, pageSize: String)

  //State management
  case class UserAuthenticationState(sessions: Map[String, UserSession] = Map.empty) {
    def updated(event: UserAuthorized): UserAuthenticationState = {
      val sessionId = UUID.randomUUID().toString;
      copy(sessions + (
        UUID.randomUUID().toString ->
          UserSession(sessionId, event.userCode, event.created, null, event.token)
        )
      )
    }

    def updated(event: Authenticated): UserAuthenticationState = {
      sessions.get(event.sessionId) match {
        case Some(session) => copy(sessions +
            (event.sessionId ->
                UserSession(event.sessionId, session.userCode,
                          session.startDate, LocalDateTime.now(),
                          session.token)
              )
        )
        case None => this
      }
    }

    def size = sessions.size
  }
  case class UserSession(sessionId: String, userCode: String,
                         startDate: LocalDateTime, endDate: LocalDateTime,
                         token: String)
}

class SessionsPersistentActor(userCode: String) extends PersistentActor with ActorLogging{
  import SessionsPersistentActor._
  override def persistenceId: String = s"authentication-history-$userCode"

  override def receiveCommand: Receive = {
    case LoginUser(loginRequest, replyTo) =>

    case InvalidateToken =>
  }

  override def receiveRecover: Receive = ???
}
