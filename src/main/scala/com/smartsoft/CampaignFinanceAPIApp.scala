package com.smartsoft

import com.smartsoft.server.CampaignFinanceAkkaHttpServer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success}

object CampaignFinanceAPIApp extends LazyLogging with APIModule {
  def main(args: Array[String]): Unit = {
    // create routes
    val routes = CampaignFinanceAPIEndpoints().allAkkaRoutes

    // Akka Management hosts the HTTP routes used by bootstrap.
    // When working with kubernetes clusters to provide readiness and liveness probe
    // AkkaManagement(system).start()

    // handle security

    // initialize server with routes and start it
    // how can I make server injectable thru some factory method.
    // we need to load server from configuration
    new CampaignFinanceAkkaHttpServer(routes).start
      .map(_.addToCoordinatedShutdown(30 seconds))
      .foreach{ server =>
        logger.info(s"Server started. HTTP Server listening on ${"localhost:8089"}")
        logger.info(s"API Documentation available on http://localhost:8089/docs")
        logger.info(s"Prometheus metrics available on http://localhost:8089/metrics")

        // handle graceful shutdown
        server.whenTerminationSignalIssued.onComplete { _ =>
          logger.info("Shutdown of HTTP service initiated")
          // shutting down the actor system. TODO: Not sure if necessary.
          terminate()
        }

        server.whenTerminated.onComplete {
          case Success(_) => logger.info("Shutdown of HTTP endpoint completed")
          case Failure(exception) => logger.error("Shutdown of HTTP endpoint failed {}", exception.getMessage)
        }
      }

  }
}
