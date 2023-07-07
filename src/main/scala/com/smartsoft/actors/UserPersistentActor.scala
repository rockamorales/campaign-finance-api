package com.smartsoft.actors

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.smartsoft.actors.SessionsPersistentActor.UserSession
import com.smartsoft.model.User
import com.smartsoft.security.{EncryptionService, JwtService, LoginRequest}

import java.time.LocalDateTime
import scala.util.{Failure, Success}

object UserPersistentActor {
  // Commands
  case class CreateUser(user: User)
  case class UpdateUser(user: User)
  case class LoginUser(loginRequest: LoginRequest)
  case class Authenticate(token: String)
  case class InvalidateAllActiveSessions(userCode: String)

  //Events
  case class UserCreated(user: User)
  case class UserUpdated(user: User)
  case class UserUnauthorized(message: String)
  case class UserAuthenticated()
  case class RegistrationFailed(exception: Throwable)

  //Query
  case class GetUser(userCode: String)
  case class GetSessionHistory(userCode: String)

  // Response
  case class UserCreatedResponse(user: User)
  case class UserUpdatedResponse(user: User)
  case class GetUserResponse(user: User)
  case class NotFound(userCode: String)

  case class UsersState(private val users: Map[String, User] = Map.empty) {
    def updated(event: UserCreated): UsersState =
      copy(users + (event.user.userCode -> event.user))

    def updated(event: UserUpdated): UsersState =
      copy(users + (event.user.userCode -> event.user))
    def getUser(userCode: String): Option[User] = users.get(userCode)
    def size: Long = users.size
  }
}

class UserPersistentActor(encryptionService: EncryptionService, jwtService: JwtService)
  extends PersistentActor with ActorLogging {
  import UserPersistentActor._

  private var usersState = UsersState()

  override def persistenceId: String = "user"

  def process(usersState: UsersState = UsersState()): Receive = {
    case CreateUser(user) =>
      //Encrypt password
      log.info(s"Created user requested: $user")
      val passwordHash = encryptionService.encrypt(user.password)
      val userWithPasswordHash = user.copy(password = passwordHash, created = Option(LocalDateTime.now()))
      val userCreatedEvent = UserCreated(userWithPasswordHash)
      persist(userCreatedEvent) { evt =>
        // remove password hash from the response
        log.info(s"persist complete")
        val user = evt.user.copy(password = "*****")
        sender() ! UserCreatedResponse(user)
        log.info(s"Message send back to sender()")
        context.become(process(usersState.updated(evt)))
      }
    case UpdateUser(user) =>
      usersState.getUser(user.userCode) match {
        case Some(_) =>
          val passwordHash = encryptionService.encrypt(user.password)
          val userWithPasswordHash = user.copy(password = passwordHash, created = Option(LocalDateTime.now()))
          val userUpdatedEvent = UserUpdated(userWithPasswordHash)
          persist(userUpdatedEvent) { evt =>
            // remove password hash from the response
//            usersState = usersState.updated(evt)
            val user = evt.user.copy(password = "*****")
            sender() ! UserUpdatedResponse(user)
            context.become(process(usersState.updated(evt)))
          }
        case None =>
          sender() ! NotFound(user.userCode)
      }

    case GetUser(userCode: String) =>
      usersState.getUser(userCode) match {
        case Some(user) =>
          val userWithoutPasswordHash = user.copy(password = "*****")
          sender() ! GetUserResponse(userWithoutPasswordHash)
        case None =>
          sender() ! NotFound(userCode)
      }

    case LoginUser(loginRequest) =>
      usersState.getUser(loginRequest.userCode) match {
        case Some(user) =>
          log.info("Child path: " + self.path.toString + s"/sessions-${user.userCode}")
          val child = context.child(s"sessions-${user.userCode}")
          child match {
            // what if child does not exists?. I will start it. Sessions actors will be created in a lazy manner
            // (until the first required interaction.
            case Some(childRef) => childRef ! SessionsPersistentActor.AuthenticateUser(loginRequest, user, sender())
            case None =>
              // if it is the first interaction with this users session actor we will create it
              // if it is down due to some issue, we will try to recover it
              val child = context.actorOf(SessionsPersistentActor.props(user.userCode, encryptionService, jwtService),
                s"sessions-${user.userCode}")
              context.watch(child)
              child ! SessionsPersistentActor.AuthenticateUser(loginRequest, user, sender())
          }
        case None => sender() ! UserUnauthorized
      }

    case Authenticate(token: String) =>
      jwtService.validateToken(token) match {
        case Failure(exception) => sender() ! UserUnauthorized(exception.getMessage)
        case Success(claims) => {
          for {
            userCode <- jwtService.getUserCode(claims)
            user <- usersState.getUser(userCode)
          } yield user
        } match {
          case Some(_) => sender() ! UserAuthenticated
          case None => sender() ! UserUnauthorized
        }
      }

    case GetSessionHistory(userCode) =>
      log.info("Child path: " + self.path.toString + s"/sessions-${userCode}")
      val child = context.child(s"sessions-${userCode}")
      child match {
        // what if child does not exists?. I will start it. Sessions actors will be created in a lazy manner
        // (until the first required interaction.
        case Some(childRef) => childRef ! SessionsPersistentActor.GetSessionHistory(userCode, sender())
        case None =>
          // if it is the first interaction with this users session actor we will create it
          // if it is down due to some issue, we will try to recover it
          val child = context.actorOf(SessionsPersistentActor.props(userCode, encryptionService, jwtService),
            s"sessions-${userCode}")
          context.watch(child)
          child ! SessionsPersistentActor.GetSessionHistory(userCode, sender())
      }

    case InvalidateAllActiveSessions(userCode) =>
      log.info("Child path: " + self.path.toString + s"/sessions-${userCode}")
      val child = context.child(s"sessions-${userCode}")
      child match {
        // what if child does not exists?. I will start it. Sessions actors will be created in a lazy manner
        // (until the first required interaction.
        case Some(childRef) => childRef ! SessionsPersistentActor.InvalidateAllActiveSessions
        case None =>
          // if it is the first interaction with this users session actor we will create it
          // if it is down due to some issue, we will try to recover it
          val child = context.actorOf(SessionsPersistentActor.props(userCode, encryptionService, jwtService),
            s"sessions-${userCode}")
          context.watch(child)
          child ! SessionsPersistentActor.InvalidateAllActiveSessions
      }


  }
  override def receiveCommand: Receive = process()

  override def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    super.onPersistFailure(cause, event, seqNr)
    sender() ! RegistrationFailed(cause)
  }

  override def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit = {
    super.onPersistRejected(cause, event, seqNr)
    sender() ! RegistrationFailed(cause)
  }

  override def receiveRecover: Receive = {
    case RecoveryCompleted =>
      log.info("Recovery complete")
      //spawn child actors?? No

    case userCreated @ UserCreated(_) =>
//      usersState = usersState.updated(userCreated)
    context.become(process(usersState.updated(userCreated)))
    case userUpdated @ UserUpdated(_) =>
      context.become(process(usersState.updated(userUpdated)))
//      usersState = usersState.updated(userUpdated)
  }
}
