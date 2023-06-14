package com.smartsoft

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

case class CampaignFinanceAPIServer(routes: Route) {
  def run(implicit actorSystem: ActorSystem) = {
    Http().newServerAt("localhost", 8089).bind(routes)
  }
}
