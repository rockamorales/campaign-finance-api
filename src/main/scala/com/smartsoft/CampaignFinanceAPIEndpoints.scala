package com.smartsoft

import akka.actor.ActorSystem
import akka.dispatch.{Dispatcher, MessageDispatcher}
import akka.http.scaladsl.server.Directives._
import com.smartsoft.controllers.CandidateController
import com.typesafe.scalalogging.LazyLogging
import sttp.apispec.openapi.circe.yaml._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.SwaggerUI

import scala.concurrent.Future

object CampaignFinanceAPIEndpoints {
  def apply(): CampaignFinanceAPIEndpoints = new CampaignFinanceAPIEndpoints
}

class CampaignFinanceAPIEndpoints() extends LazyLogging with APIModule {
  implicit val dispatcher: MessageDispatcher =
    actorSystem.dispatchers.lookup("akka-http-server-interpreter-dispatcher")

  val prometheusMetrics: PrometheusMetrics[Future] = PrometheusMetrics.default[Future]()
  val metricsEndpoint: ServerEndpoint[Any, Future] = prometheusMetrics.metricsEndpoint
  val customServerOptions = AkkaHttpServerOptions.customiseInterceptors
    .metricsInterceptor(prometheusMetrics.metricsInterceptor()).options

  val docs = OpenAPIDocsInterpreter()
    .toOpenAPI(authController.allServerEndpoints.map(_.endpoint),
          "Campaign Finances", "1.0")

  val allAkkaRoutes =
    AkkaHttpServerInterpreter(customServerOptions).toRoute(metricsEndpoint) ~
      AkkaHttpServerInterpreter(customServerOptions).toRoute(SwaggerUI[Future](docs.toYaml)) ~
      AkkaHttpServerInterpreter(customServerOptions).toRoute(authController.allServerEndpoints)
}
