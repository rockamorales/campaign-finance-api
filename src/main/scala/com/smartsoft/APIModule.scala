package com.smartsoft

import akka.actor.{ActorSystem, Terminated}
import com.smartsoft.actors.{SessionsPersistentActor, UserPersistentActor}
import com.smartsoft.controllers.{AuthController, CandidateController}
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

  lazy val apiSecurity = wire[APISecurity]
  lazy val authService = wire[AuthenticationService]
  lazy val jwtService = wire[JwtService]
  lazy val candidatesService = wire[CandidatesService]
  lazy val encryptionService = wire[EncryptionService]
  lazy val userPersistentActor = wireActor[UserPersistentActor]("users")
  lazy val authController = wire[AuthController]
  lazy val userService = wire[UserService]
  def createServer(impl: String) = wireWith(APIServer.create _ )

  def terminate(): Future[Terminated] = {
    actorSystem.terminate()
  }


}
