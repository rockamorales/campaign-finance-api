package com.smartsoft.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import com.smartsoft.model.User
import com.smartsoft.security.{EncryptionService, JwtService, LoginRequest}
import com.typesafe.config.Config

object UserManagementActor {
  abstract class Command(key: String) extends ConsistentHashable {
    override def consistentHashKey: Any = key
  }

  case class CreateUser(user: User) extends Command(user.userCode)

  case class UpdateUser(user: User) extends Command(user.userCode)

  case class LoginUser(loginRequest: LoginRequest) extends Command(loginRequest.userCode)

  case class Authenticate(token: String, userCode: String) extends Command(userCode)

  case class InvalidateAllActiveSessions(userCode: String) extends Command(userCode)

  //Query
  abstract class Query(key: String) extends ConsistentHashable {
    override def consistentHashKey: Any = key
  }

  case class GetSessionHistory(userCode: String) extends Query(userCode)

}

class UserManagementActor(config: Config, encryptionService: EncryptionService, jwtService: JwtService)
  extends Actor with ActorLogging {

  import UserManagementActor._

  override def receive: Receive = {
    case CreateUser(user) =>
        getChild(user.userCode).forward(
          UserPersistentActor.CreateUser(user)
        )
    case UpdateUser(user) =>
      getChild(user.userCode)
        .forward(UserPersistentActor.UpdateUser(user))
    case LoginUser(loginRequest) =>
      getChild(loginRequest.userCode)
        .forward(UserPersistentActor.LoginUser(loginRequest))
    case Authenticate(token, userCode) =>
      getChild(userCode)
        .forward(UserPersistentActor.Authenticate(token, userCode))
    case InvalidateAllActiveSessions(userCode) =>
      getChild(userCode)
        .forward(UserPersistentActor.InvalidateAllActiveSessions(userCode))
    case GetSessionHistory(userCode) =>
      getChild(userCode)
        .forward(UserPersistentActor.GetSessionHistory(userCode))
  }

  def getChild(userCode: String): ActorRef = {
    context.child(s"user-$userCode") match {
      case Some(actor) => actor
      case None =>
        //spawn the actor, might be first time is required, or maybe the actor crashed
        // or some cluster node crashed
        context.actorOf(
          UserPersistentActor.props(userCode, encryptionService, jwtService),
        s"user-$userCode"
        )
    }
  }
}
