package com.smartsoft.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.smartsoft.CampaignFinanceAPIEndpoints
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object CampaignFinanceAkkaHttpServer {
  def apply[T]()(implicit actorSystem: ActorSystem, config: Config, ec: ExecutionContext): APIServer[Future] = {
    new CampaignFinanceAkkaHttpServer()
  }
}
class CampaignFinanceAkkaHttpServer()(implicit val actorSystem: ActorSystem, config: Config, ec: ExecutionContext)
  extends APIServer[Future] with LazyLogging{
    def start() = {
      val host: String = config.getString("server.http.host")
      val port: Int = config.getInt("server.http.port")
      val httpBinding = Http()
        .newServerAt(host, port)
        .bind(CampaignFinanceAPIEndpoints().allAkkaRoutes)


        httpBinding.map(_.addToCoordinatedShutdown(30 seconds))
        .foreach { server =>
          logger.info(s"Server started. HTTP Server listening on ${s"$host:$port"}")
          logger.info(s"API Documentation available on http://$host:$port/docs")
          logger.info(s"Prometheus metrics available on http://$host:$port/metrics")

          // handle graceful shutdown
          server.whenTerminationSignalIssued.onComplete { _ =>
            logger.info("Shutdown of HTTP service initiated")
            // should we shut down the actor system?
          }

          server.whenTerminated.onComplete {
            case Success(_) => logger.info("Shutdown of HTTP endpoint completed")
            case Failure(exception) => logger.error("Shutdown of HTTP endpoint failed {}", exception.getMessage)
          }
        }
    }
}