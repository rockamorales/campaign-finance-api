package com.smartsoft

import akka.actor.{ActorSystem, Terminated}
import com.smartsoft.controllers.CandidateController
import com.smartsoft.server.APIServer
import com.softwaremill.macwire.wire
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future

trait APIModule {

  implicit val actorSystem = ActorSystem("CampaignFinanceServerAPI")

  lazy val config: Config = ConfigFactory.load()
  import actorSystem.dispatcher

  lazy val candidateController = wire[CandidateController]

  def terminate(): Future[Terminated] = {
    actorSystem.terminate()
  }


}
