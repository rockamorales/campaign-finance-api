package com.smartsoft.controllers

import com.smartsoft.model.{Candidate, ErrorInfo, User}
import com.smartsoft.security.APISecurity
import com.smartsoft.services.CandidatesService
import sttp.tapir._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.server.{PartialServerEndpoint, ServerEndpoint}

import scala.concurrent.{ExecutionContext, Future}

class CandidateController(apiSecurity: APISecurity, candidatesService: CandidatesService)(implicit ec: ExecutionContext) extends APIController {

  val candidateBaseEndpoint: PartialServerEndpoint[String, User, Unit, ErrorInfo, Unit, Any, Future]
  = apiSecurity.withSecurity(baseEndpoint)
    .in("candidate").description("Candidate Endpoints: Provides all functionality to manage candidates catalogue data")
  // in(path[Int]("cid").and(jsonBody[Candidate]))
  val candidateCreateEndpoint: ServerEndpoint.Full[String, User, Candidate, ErrorInfo, Candidate, Any, Future] =
    candidateBaseEndpoint
      .post
      .description("Creates a new candidate")
      .in(jsonBody[Candidate]
        .description("Candidate's information to be persisted"))
      .out(jsonBody[Candidate]
        .description("Persisted candidates information"))
      .serverLogic(_ => candidatesService.createCandidate _)
      // first parameter is ignored for now, user is not used, maybe we should use it for audit
  //   ^-- TODO: probably we should return empty body with 204 - No Content,
  //    because we are not generating any new data on the server

  val candidateUpdateEndpoint: ServerEndpoint.Full[String, User, (String, Candidate), ErrorInfo, StatusCode, Any, Future] =
    candidateBaseEndpoint
      .put
      .description("Updates candidates information")
      .in(path[String]("cid")
        .description("Candidate Id"))
      .in(jsonBody[Candidate]
        .description("Candidate updated information that must be persisted. " +
          "CID cannot be changed. If CID does not exists an HTTP - 404 - Not Found will be returned"))
      .out(jsonBody[StatusCode]
        .description("If request is executed without errors: 204 - No Content HTTP will be returned"))
      .serverLogic(_ => (candidatesService.updateCandidate _).tupled)

  val candidateByID: ServerEndpoint.Full[String, User, String, ErrorInfo, Candidate, Any, Future] =
    candidateBaseEndpoint
      .get
      .description("Retrieves candidate's data by ID")
      .in(path[String]("cid")
        .description("Candidate Id"))
      .out(jsonBody[Candidate]
        .description("Candidate information. Will return 404 - Not Found if the candidate ID is not found"))
      .serverLogic(_ => candidatesService.findCandidateById _)

  val candidateListAll: ServerEndpoint.Full[String, User, Unit, ErrorInfo, List[Candidate], Any, Future] =
    candidateBaseEndpoint
      .get
      .description("Retrieves all candidates.")
      .out(jsonBody[List[Candidate]]
        .description("A list of candidates"))
      .serverLogic(_ => _ => candidatesService.findAllCandidate())


  val allServerEndpoints = List(
    candidateCreateEndpoint,
    candidateUpdateEndpoint,
    candidateByID,
    candidateListAll
  )
}
