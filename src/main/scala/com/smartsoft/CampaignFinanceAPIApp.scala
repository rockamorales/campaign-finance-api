package com.smartsoft

import com.typesafe.scalalogging.LazyLogging

import scala.language.postfixOps

object CampaignFinanceAPIApp extends LazyLogging with APIModule {
  def main(args: Array[String]): Unit = {
    // create routes
//    val routes = CampaignFinanceAPIEndpoints().allAkkaRoutes

    // Akka Management hosts the HTTP routes used by bootstrap.
    // When working with kubernetes clusters to provide readiness and liveness probe
    // AkkaManagement(system).start()

    // handle security

    // initialize server with routes and start it
    // how can I make server injectable thru some factory method.
    // we need to load server from configuration
    createServer("akka-http").start
  }
}
