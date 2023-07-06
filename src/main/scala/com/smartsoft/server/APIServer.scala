package com.smartsoft.server

import akka.actor.ActorSystem
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

trait APIServer[F[_]] {
  def start()
}

object APIServer {
  def create(implementation: String)
            (implicit system: ActorSystem, config: Config, ec: ExecutionContext)
      = implementation match {
        case "akka-http" =>
          CampaignFinanceAkkaHttpServer()
        case _ =>
          throw new RuntimeException(s"Unsupported server implementation: $implementation")
  }
}