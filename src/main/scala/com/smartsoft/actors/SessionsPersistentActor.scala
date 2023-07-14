package com.smartsoft.actors

import akka.actor.{ActorLogging, ActorRef, Props, Timers}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.smartsoft.actors.SessionStates.SessionStates
import com.smartsoft.model.User
import com.smartsoft.security.{EncryptionService, JwtService, LoginRequest}

import java.time.LocalDateTime
import java.util.UUID
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.language.postfixOps

object SessionsPersistentActor {
  def props(userCode: String, encryptionService: EncryptionService, jwtService: JwtService): Props = {
    Props(new SessionsPersistentActor(userCode, encryptionService, jwtService))
  }

  //Commands
  case class AuthenticateUser(loginRequest: LoginRequest, user: User,  replyTo: ActorRef)
  case class InvalidateToken(token: String)
  case class InvalidateSession(userSession: UserSession, replyTo: ActorRef)
  case class AuthorizeUser(sessionId: String, replyTo: ActorRef, user: User)
  object UpdateExpiredSessions

  object InvalidateAllActiveSessions

  //Events
  case class UserAuthenticated(userSession: UserSession)
  case class UserAuthorized(user: User)
  case class UserUnauthorized(message: String)
  case class SessionExpired(userSession: UserSession)

  case class UserAuthenticationFailed(userSession: UserSession)
  object AllActiveSessionsInvalidated
  case class SessionInvalidated(sessionDetails: UserSession)


  //Query
  object GetActiveSessions
  case class GetSessionDetails(sessionId: String)
  case class GetSessionHistory(userCode: String, replyTo: ActorRef)

  case class GetPaginatedSessionHistory(page: String, pageSize: String)

  // Responses
  case class SessionHistoryResponse(sessions: List[UserSession])

  //State management
  case class SessionsState(sessions: Map[String, UserSession] = Map.empty) {
    def updated(userSession: UserSession): SessionsState = {
      copy(sessions + (
        userSession.sessionId ->
          userSession
        )
      )
    }

    def updated(sessionExpiredEvent: SessionExpired): SessionsState = {
      copy(sessions + (
        sessionExpiredEvent.userSession.sessionId ->
          sessionExpiredEvent.userSession
        )
      )
    }

    def updated(sessionInvalidated: SessionInvalidated): SessionsState = {
      copy(sessions + (
        sessionInvalidated.sessionDetails.sessionId ->
          sessionInvalidated.sessionDetails
        )
      )
    }

    def get(key: String) = {
        sessions.get(key)
    }
    def size = sessions.size
  }
  case class UserSession(sessionId: String, userCode: String,
                         startDate: LocalDateTime, endDate: LocalDateTime,
                         token: String, state: SessionStates, message: String)
}

class SessionsPersistentActor(userCode: String, encryptionService: EncryptionService, jwtService: JwtService)
  extends Timers with PersistentActor with ActorLogging {

  import SessionsPersistentActor._

  var sessionsState = SessionsState()
  val timerKey = UUID.randomUUID().toString
  timers.startTimerWithFixedDelay(timerKey, UpdateExpiredSessions, 30 seconds)

  override def persistenceId: String = s"authentication-history-$userCode"

  override def receiveCommand: Receive = handleReceiveCommand()

  def handleReceiveCommand(sessionsState: SessionsState = SessionsState()): Receive = {
    case AuthorizeUser(sessionId, replyTo, user) =>
      sessionsState.get(sessionId).filter(_.state == SessionStates.ACTIVE) match {
        case Some(_) =>
          val userWithoutPassHash = user.copy(password = "****")
          replyTo ! UserAuthorized(userWithoutPassHash)
        case None =>
          replyTo ! UserUnauthorized("Invalid session / Session expired")
      }

    case UpdateExpiredSessions =>
      log.info("Scheduled Expired sessions update started")
      val expiredSessions = sessionsState.sessions.values
        .filter(_.state == SessionStates.ACTIVE)
        .filter(userSession => jwtService.validateToken(userSession.token).isFailure)
        .map(session => SessionExpired(
          session.copy(
            endDate = LocalDateTime.now,
            state = SessionStates.EXPIRED,
            message = "Session expired"

          )
        ))
        .toList

      persistAll(expiredSessions) {
        expiredSessionEvt =>
          context.become(handleReceiveCommand(sessionsState.updated(expiredSessionEvt)))
      }

    case AuthenticateUser(loginRequest, user, replyTo) =>
      log.info(s"User authentication requested: ${loginRequest.userCode}")
      encryptionService.checkPassword(loginRequest.password, user.password) match {
        case Success(result) =>
          if (result) {
            log.info(s"User successfully authenticated: ${loginRequest.userCode}")
            val sessionId = UUID.randomUUID().toString
            val authenticatedEvent = UserAuthenticated(UserSession(sessionId, loginRequest.userCode,
              LocalDateTime.now, null, jwtService.generateToken(user.userCode, sessionId), SessionStates.ACTIVE,
              "Successful authentication"))
            persistAsync(authenticatedEvent) { persistedEvt =>
              replyTo ! UserAuthenticated(authenticatedEvent.userSession)
              context.become(handleReceiveCommand(sessionsState.updated(persistedEvt.userSession)))
            }
          } else {
            handleAuthenticationFailure(s"Invalid password for user: ${loginRequest.userCode}", loginRequest, replyTo)
          }
        case Failure(exception) =>
          handleAuthenticationFailure(s"Authentication failed: ${exception.getMessage}", loginRequest, replyTo)
      }

    case GetSessionHistory(_, replyTo) =>
      replyTo ! SessionHistoryResponse(sessionsState.sessions.values.toList)

    case InvalidateToken(token) =>
      log.info(s"InvalidateToken requested: $token")

    case InvalidateSession(userSession, replyTo) =>
      log.info(s"InvalidateSession requested: $userSession")
      val sessionInvalidated = SessionInvalidated(userSession.copy(state = SessionStates.INVALIDATED, endDate = LocalDateTime.now,
        message = "Session Invalidation requested"))

      persist(sessionInvalidated) {
        sessionInvalidatedPersistedEvt =>
          context.become(handleReceiveCommand(sessionsState.updated(sessionInvalidatedPersistedEvt)))
      }

    case InvalidateAllActiveSessions =>
      log.info(s"InvalidateAllActiveSessions requested")
      for {
        session <- sessionsState.sessions.values if (session.state == SessionStates.ACTIVE)
      } self ! InvalidateSession(session, sender())

  }
  def handleAuthenticationFailure(message: String, loginRequest: LoginRequest, replyTo: ActorRef) = {
    log.error(s"Authentication failed: $message")
    val failedEvent = UserAuthenticationFailed(UserSession(UUID.randomUUID().toString, loginRequest.userCode,
      LocalDateTime.now(), null, null, SessionStates.UNAUTHORIZED, message))

    persistAsync(failedEvent) { persistedEvt =>
      context.become(handleReceiveCommand(sessionsState.updated(persistedEvt.userSession)))
    }
    replyTo ! UserAuthenticationFailed(failedEvent.userSession)
  }

  override def receiveRecover: Receive = {
    case RecoveryCompleted =>
      log.info("Session history recovery completed")
    case sessionExpired @ SessionExpired(_) =>
      context.become(handleReceiveCommand(sessionsState.updated(sessionExpired)))
    case UserAuthenticationFailed(userSession) =>
      context.become(handleReceiveCommand(sessionsState.updated(userSession)))
    case UserAuthenticated(userSession) =>
      context.become(handleReceiveCommand(sessionsState.updated(userSession)))
    case sessionInvalidated @ SessionInvalidated(_) =>
      context.become(handleReceiveCommand(sessionsState.updated(sessionInvalidated)))
  }
}

object SessionStates extends Enumeration {
  type SessionStates = Value
  val UNAUTHORIZED, ACTIVE, EXPIRED, INVALIDATED = Value
}

