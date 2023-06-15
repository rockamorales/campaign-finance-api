package com.smartsoft.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.concurrent.Future

class CampaignFinanceAkkaHttpServer(routes: Route)(implicit val actorSystem: ActorSystem) extends APIServer[Future] {
  type RoutesType = Route
  def start: Future[Http.ServerBinding] = {
    Http().newServerAt("localhost", 8089).bind(routes)
  }
}