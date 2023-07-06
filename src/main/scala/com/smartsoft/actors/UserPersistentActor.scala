package com.smartsoft.actors

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.smartsoft.model.{LoginResponse, User}
import com.smartsoft.security.{EncryptionService, JwtService, LoginRequest}

import java.time.LocalDateTime
import scala.util.{Failure, Success}

object UserPersistentActor {
  // Commands
  case class CreateUser(user: User)
  case class UpdateUser(user: User)
  case class LoginUser(loginRequest: LoginRequest)
  case class Authenticate(token: String)

  //Events
  case class UserCreated(user: User)
  case class UserUpdated(user: User)
  case class UserUnauthorized(message: String)
  case class UserAuthenticated()

  //Query
  case class GetUser(userCode: String)

  // Response
  case class UserCreatedResponse(user: User);
  case class UserUpdatedResponse(user: User);
  case class GetUserResponse(user: User);
  case class NotFound(userCode: String);

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

  var usersState = UsersState()

  override def persistenceId: String = "user"

  override def receiveCommand: Receive = {
    case CreateUser(user) =>
      //Encrypt password
      val passwordHash = encryptionService.encrypt(user.password);
      val userWithPasswordHash = user.copy(password = passwordHash, created = LocalDateTime.now())
      val userCreatedEvent = UserCreated(userWithPasswordHash)
      persist(userCreatedEvent) { evt =>
        usersState = usersState.updated(evt)
        // remove password hash from the response
        val user = evt.user.copy(password = "*****")
        sender() ! UserCreatedResponse(user)
      }
    case UpdateUser(user) =>
      usersState.getUser(user.userCode) match {
        case Some(_) =>
          val passwordHash = encryptionService.encrypt(user.password)
          val userWithPasswordHash = user.copy(password = passwordHash, created = LocalDateTime.now())
          val userUpdatedEvent = UserUpdated(userWithPasswordHash)
          persist(userUpdatedEvent) { evt =>
            // remove password hash from the response
            usersState = usersState.updated(evt)
            val user = evt.user.copy(password = "*****")
            sender() ! UserUpdatedResponse(user)
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
              case Some(childRef) => childRef ! SessionsPersistentActor.LoginUser(loginRequest, sender())
              case None =>
                // if it is the first interaction with this users session actor we will create it
                // if it is down due to some issue, we will try to recover it
                val child = context.actorOf(SessionsPersistentActor.props(user.userCode),
                                            s"sessions-${user.userCode}")
                context.watch(child)
                child ! SessionsPersistentActor.LoginUser(loginRequest, sender())
            }
        case None => sender() ! UserUnauthorized
      }

    case Authenticate(token: String) => {
       jwtService.validateToken(token) match {
        case Failure(exception) => sender() ! UserUnauthorized(exception.getMessage)
        case Success(claims) =>
          {
            for {
              userCode <- jwtService.getUserCode(claims)
              user <- usersState.getUser(userCode)
            } yield user
          } match {
            case Some(_) => sender() ! UserAuthenticated
            case None => sender() ! UserUnauthorized
          }
      }
    }

  }

  override def receiveRecover: Receive = {
    case RecoveryCompleted =>
      //spawn child actors??

    case userCreated @ UserCreated(_) =>
      usersState = usersState.updated(userCreated)
      context.actorOf(SessionsPersistentActor.props(userCreated.user.userCode))
    case userUpdated @ UserUpdated(_) =>
      usersState = usersState.updated(userUpdated)
  }
}
