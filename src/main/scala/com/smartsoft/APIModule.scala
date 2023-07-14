package com.smartsoft

import akka.actor.{ActorRef, ActorSystem, Terminated}
import com.smartsoft.actors.{UserManagementActor, UserPersistentActor, UsersSupervisorActor}
import com.smartsoft.controllers.{AuthController, UsersController}
import com.smartsoft.security.{APISecurity, AuthenticationService, EncryptionService, JwtService}
import com.smartsoft.server.APIServer
import com.smartsoft.services.{CandidatesService, UserService}
import com.typesafe.config.{Config, ConfigFactory}
import com.softwaremill.macwire._
import com.softwaremill.macwire.akkasupport._

import scala.concurrent.Future

trait APIModule {

  implicit lazy val actorSystem: ActorSystem = ActorSystem("CampaignFinanceServerAPI")

  implicit lazy val config: Config = ConfigFactory.load()
  import actorSystem.dispatcher

  lazy val apiSecurity: APISecurity = wire[APISecurity]
  lazy val authService: AuthenticationService = wire[AuthenticationService]
  lazy val jwtService: JwtService = wire[JwtService]
  lazy val candidatesService: CandidatesService = wire[CandidatesService]
  lazy val encryptionService: EncryptionService = wire[EncryptionService]

  lazy val userManagementActorProps = wireProps[UserManagementActor]
  lazy val usersSupervisorActor = wireActor[UsersSupervisorActor]("users-supervisor")
  lazy val authController: AuthController = wire[AuthController]
  lazy val userService: UserService = wire[UserService]
  lazy val usersController: UsersController = wire[UsersController]

  def createServer(impl: String): APIServer[Future] = wireWith(APIServer.create _)

  def terminate(): Future[Terminated] = {
    actorSystem.terminate()
  }


}
