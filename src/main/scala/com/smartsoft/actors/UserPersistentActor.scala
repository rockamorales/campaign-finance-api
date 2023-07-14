package com.smartsoft.actors

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.smartsoft.model.User
import com.smartsoft.security.{EncryptionService, JwtService, LoginRequest}

import java.time.LocalDateTime
import scala.util.{Failure, Success}

object UserPersistentActor {

  def create(userCode: String, encryptionService: EncryptionService, jwtService: JwtService) = {
    new UserPersistentActor(userCode, encryptionService, jwtService)
  }
  def props(userCode: String, encryptionService: EncryptionService, jwtService: JwtService): Props = {
    Props(new UserPersistentActor(userCode, encryptionService, jwtService))
  }
  // Commands
  case class CreateUser(user: User)
  case class UpdateUser(user: User)
  case class LoginUser(loginRequest: LoginRequest)
  case class Authenticate(token: String, userCode: String)
  case class InvalidateAllActiveSessions(userCode: String)

  //Events
  case class UserCreated(user: User)
  case class UserUpdated(user: User)
  case class UserUnauthorized(message: String)
  case class UserAuthenticated(user: User)
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

class UserPersistentActor(userCode: String, encryptionService: EncryptionService, jwtService: JwtService)
  extends PersistentActor with ActorLogging {
  import UserPersistentActor._

  private var usersState = UsersState()

  override def persistenceId: String = s"user-$userCode"

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
          getChild(loginRequest.userCode) ! SessionsPersistentActor.AuthenticateUser(loginRequest, user, sender())
//          log.info("Child path: " + self.path.toString + s"/sessions-${user.userCode}")
//          val child = context.child(s"sessions-${user.userCode}")
//          child match {
//            // what if child does not exists?. I will start it. Sessions actors will be created in a lazy manner
//            // (until the first required interaction.
//            case Some(childRef) => childRef ! SessionsPersistentActor.AuthenticateUser(loginRequest, user, sender())
//            case None =>
//              // if it is the first interaction with this users session actor we will create it
//              // if it is down due to some issue, we will try to recover it
//              val child = context.actorOf(SessionsPersistentActor.props(user.userCode, encryptionService, jwtService),
//                s"sessions-${user.userCode}")
//              context.watch(child)
//              child ! SessionsPersistentActor.AuthenticateUser(loginRequest, user, sender())
//          }
        case None => sender() ! UserUnauthorized("User unauthorized")
      }

    case Authenticate(token: String, userCode: String) =>
      jwtService.validateToken(token) match {
        case Failure(exception) => sender() ! UserUnauthorized(exception.getMessage)
        case Success(claims) =>
          val userAndSessionId =  for {
              user <- usersState.getUser(userCode)
              sessionId <- jwtService.getSessionId(claims)
            } yield (user, sessionId)

          userAndSessionId match {
            case Some((user, sessionId)) =>
              getChild(user.userCode) ! SessionsPersistentActor.AuthorizeUser(sessionId, sender(), user)
              //sender() ! UserAuthenticated(user)
            case None => sender() ! UserUnauthorized("Token invalid or expired")
          }
      }

    case GetSessionHistory(userCode) =>
      getChild(userCode) ! SessionsPersistentActor.GetSessionHistory(userCode, sender())
//      log.info("Child path: " + self.path.toString + s"/sessions-${userCode}")
//      val child = context.child(s"sessions-${userCode}")
//      child match {
//        // what if child does not exists?. I will start it. Sessions actors will be created in a lazy manner
//        // (until the first required interaction.
//        case Some(childRef) => childRef ! SessionsPersistentActor.GetSessionHistory(userCode, sender())
//        case None =>
//          // if it is the first interaction with this users session actor we will create it
//          // if it is down due to some issue, we will try to recover it
//          val child = context.actorOf(SessionsPersistentActor.props(userCode, encryptionService, jwtService),
//            s"sessions-${userCode}")
//          context.watch(child)
//          child ! SessionsPersistentActor.GetSessionHistory(userCode, sender())
//      }

    case InvalidateAllActiveSessions(userCode) =>
      getChild(userCode) ! SessionsPersistentActor.InvalidateAllActiveSessions
//      log.info("Child path: " + self.path.toString + s"/sessions-${userCode}")
//      val child = context.child(s"sessions-${userCode}")
//      child match {
//        // what if child does not exists?. I will start it. Sessions actors will be created in a lazy manner
//        // (until the first required interaction.
//        case Some(childRef) => childRef ! SessionsPersistentActor.InvalidateAllActiveSessions
//        case None =>
//          // if it is the first interaction with this users session actor we will create it
//          // if it is down due to some issue, we will try to recover it
//          val child = context.actorOf(SessionsPersistentActor.props(userCode, encryptionService, jwtService),
//            s"sessions-${userCode}")
//          context.watch(child)
//          child ! SessionsPersistentActor.InvalidateAllActiveSessions
//      }


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
    context.become(process(usersState.updated(userCreated)))
    case userUpdated @ UserUpdated(_) =>
      context.become(process(usersState.updated(userUpdated)))
  }

  def getChild(userCode: String): ActorRef = {
    context.child(s"user-$userCode") match {
      case Some(actor) => actor
      case None =>
        //spawn the actor, might be first time is required, or maybe the actor crashed
        // or some cluster node crashed
        context.actorOf(
          SessionsPersistentActor.props(userCode, encryptionService, jwtService),
          s"user-$userCode"
        )
    }
  }

}
