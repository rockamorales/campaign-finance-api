package com.smartsoft.server

import akka.actor.ActorSystem
import akka.http.javadsl.server.Route
import akka.http.scaladsl.server
import com.smartsoft.model.{Candidate, ErrorInfo, User}
import com.typesafe.config.Config
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.{PartialServerEndpoint, ServerEndpoint}

import scala.concurrent.ExecutionContext

trait APIServer[F[_]] {
  def start()
}

object APIServer {
  def create(implementation: String)(implicit system: ActorSystem, config: Config, ec: ExecutionContext) =
    implementation match {

      case "akka-http" =>
        CampaignFinanceAkkaHttpServer() //.asInstanceOf[APIServer[_]]
      case _ =>
        throw new RuntimeException(s"Unsupported server implementation: $implementation")
  }
}