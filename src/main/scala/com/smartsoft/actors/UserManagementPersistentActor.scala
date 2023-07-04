package com.smartsoft.actors

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import com.smartsoft.model.User
import com.smartsoft.security.EncryptionService

import java.time.{LocalDateTime}

object UserManagementPersistentActor {
  // Commands
  case class CreateUser(user: User)
  case class UpdateUser(user: User)

  //Events
  case class UserCreated(user: User)
  case class UserUpdated(user: User)

  //Query
  case class GetUser(userCode: String)

  // Response
  case class UserCreatedResponse(user: User);
  case class UserUpdatedResponse(user: User);
  case class GetUserResponse(user: User);
  case class NotFound(userCode: String);

  case class UsersState(users: Map[String, User] = Map.empty) {
    def updated(event: UserCreated): UsersState =
      copy(users + (event.user.userCode -> event.user))

    def updated(event: UserUpdated): UsersState =
      copy(users + (event.user.userCode -> event.user))

    def size: Long = users.size
  }
}

class UserManagementPersistentActor(encryptionService: EncryptionService)
  extends PersistentActor with ActorLogging {
  import UserManagementPersistentActor._

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
      usersState.users.get(user.userCode) match {
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
      usersState.users.get(userCode) match {
        case Some(user) =>
          val userWithoutPasswordHash = user.copy(password = "*****")
          sender() ! GetUserResponse(userWithoutPasswordHash)
        case None =>
          sender() ! NotFound(userCode)
      }
  }

  override def receiveRecover: Receive = {
    case userCreated @ UserCreated(_) =>
      usersState = usersState.updated(userCreated)
    case userUpdated @ UserUpdated(_) =>
      usersState = usersState.updated(userUpdated)
  }
}
