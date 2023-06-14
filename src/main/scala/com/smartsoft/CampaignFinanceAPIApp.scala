package com.smartsoft

import akka.actor.ActorSystem
import akka.event.Logging
import com.smartsoft.routes.CampaignFinanceAPIEndpoints

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success}

object CampaignFinanceAPIApp extends{
  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("CampaignFinanceServerAPI")

    val log = Logging(system.eventStream, "CampaignFinanceAPIApp")
    // create routes
    val routes = CampaignFinanceAPIEndpoints(system).allAkkaRoutes

    // Akka Management hosts the HTTP routes used by bootstrap.
    // When working with kubernetes clusters to provide readiness and liveness probe
    // AkkaManagement(system).start()

    // initialize server with routes and start it
    CampaignFinanceAPIServer(routes).run
      .map(_.addToCoordinatedShutdown(30 seconds))
      .foreach{ server =>
        log.info(s"Server started. HTTP Server listening on ${"localhost:8089"}")
        log.info(s"API Documentation available on http://localhost:8089/docs")
        log.info(s"Prometheus metrics available on http://localhost:8089/metrics")

        // handle graceful shutdown
        server.whenTerminationSignalIssued.onComplete { _ =>
          log.info("Shutdown of HTTP service initiated")
          // shutting down the actor system. TODO: Not sure if necessary.
          system.terminate()
        }

        server.whenTerminated.onComplete {
          case Success(_) => log.info("Shutdown of HTTP endpoint completed")
          case Failure(exception) => log.error("Shutdown of HTTP endpoint failed {}", exception.getMessage)
        }
      }

  }
}
