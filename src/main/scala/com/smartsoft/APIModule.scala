package com.smartsoft

import akka.actor.{ActorSystem, Terminated}
import com.smartsoft.controllers.CandidateController
import com.smartsoft.security.{APISecurity, AuthenticationService, JwtService}
import com.smartsoft.server.APIServer
import com.smartsoft.services.CandidatesService
//import com.softwaremill.macwire.{wire, wireWith}
import com.typesafe.config.{Config, ConfigFactory}
import com.softwaremill.macwire._

import scala.concurrent.Future

trait APIModule {

  implicit lazy val actorSystem: ActorSystem = ActorSystem("CampaignFinanceServerAPI")

  implicit lazy val config: Config = ConfigFactory.load()
  import actorSystem.dispatcher

  lazy val candidateController = wire[CandidateController]
  lazy val apiSecurity = wire[APISecurity]
  lazy val authService = wire[AuthenticationService]
  lazy val jwtService = wire[JwtService]
  lazy val candidatesService = wire[CandidatesService]
  def createServer(impl: String) = wireWith(APIServer.create _ )

  def terminate(): Future[Terminated] = {
    actorSystem.terminate()
  }


}
