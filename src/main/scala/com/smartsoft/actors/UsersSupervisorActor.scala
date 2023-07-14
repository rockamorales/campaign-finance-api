package com.smartsoft.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import akka.routing.FromConfig
import com.smartsoft.model.User
import com.smartsoft.security.{JwtService, LoginRequest}
import com.typesafe.config.Config

import scala.util.{Failure, Success}

object UsersSupervisorActor {
  abstract class Command(key: String) extends ConsistentHashable {
    override def consistentHashKey: Any = key
  }

  case class CreateUser(user: User) extends Command(user.userCode)

  case class UpdateUser(user: User) extends Command(user.userCode)

  case class LoginUser(loginRequest: LoginRequest) extends Command(loginRequest.userCode)

  case class Authenticate(token: String)

  case class InvalidateAllActiveSessions(userCode: String)

  case class GetSessionHistory(userCode: String)

  // Events
  case class UserUnauthorized(message: String)
}

class UsersSupervisorActor(config: Config, jwtService: JwtService, userManagementActorProps: Props) extends Actor with ActorLogging {
  import UsersSupervisorActor._

  val usersRouter: ActorRef =
    context.actorOf(FromConfig.props(userManagementActorProps), "users-manager-router")

  override def receive: Receive = {
    case CreateUser(user) => usersRouter.forward(UserManagementActor.CreateUser(user))
    case UpdateUser(user) => usersRouter.forward(UserManagementActor.UpdateUser(user))
    case LoginUser(loginRequest) => usersRouter.forward(UserManagementActor.LoginUser(loginRequest))
    case Authenticate(token) =>
      jwtService.getUserCode(token) match {
        case Success(userCodeOpt) =>
          if (userCodeOpt.isEmpty) {
            sender() ! UserUnauthorized("Invalid user/password")
          } else {
            usersRouter.forward(UserManagementActor.Authenticate(token, userCodeOpt.get))
          }
        case Failure(exception) =>
          log.error(exception, "Token parser failed: {}", exception.getMessage)
          sender() ! UserUnauthorized("Token parser failed: " + exception.getMessage)
      }
    case InvalidateAllActiveSessions(userCode) =>
      usersRouter.forward(UserManagementActor.InvalidateAllActiveSessions(userCode))
    case GetSessionHistory(userCode) => usersRouter.forward(UserManagementActor.GetSessionHistory(userCode))
  }
}
