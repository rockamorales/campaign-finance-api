package com.smartsoft.routes

import akka.actor.ActorSystem
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.SwaggerUI
import akka.http.scaladsl.server.Directives._
import sttp.apispec.openapi.circe.yaml._

import scala.concurrent.Future
object CampaignFinanceAPIEndpoints {
  def apply(implicit system: ActorSystem): CampaignFinanceAPIEndpoints = new CampaignFinanceAPIEndpoints
}

class CampaignFinanceAPIEndpoints(implicit val system: ActorSystem) {
  implicit val dispatcher = system.dispatchers.lookup("akka-http-server-interpreter-dispatcher")

  val prometheusMetrics: PrometheusMetrics[Future] = PrometheusMetrics.default[Future]()
  val metricsEndpoint: ServerEndpoint[Any, Future] = prometheusMetrics.metricsEndpoint
  val customServerOptions = AkkaHttpServerOptions.customiseInterceptors
    .metricsInterceptor(prometheusMetrics.metricsInterceptor()).options

  val docs = OpenAPIDocsInterpreter().toOpenAPI(CandidateEndpoints.allEndpoints, "Campaign Finances", "1.0")
  val allAkkaRoutes =
    AkkaHttpServerInterpreter(customServerOptions).toRoute(metricsEndpoint) ~
      AkkaHttpServerInterpreter(customServerOptions).toRoute(SwaggerUI[Future](docs.toYaml)) ~
      AkkaHttpServerInterpreter(customServerOptions).toRoute(CandidateEndpoints.allServerEndpoints)
}
