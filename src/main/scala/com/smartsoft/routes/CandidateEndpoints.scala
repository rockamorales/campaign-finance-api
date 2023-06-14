package com.smartsoft.routes

import akka.actor.ActorSystem
import com.smartsoft.model.{Candidate, ErrorInfo}
import com.smartsoft.services.CandidatesService
import sttp.tapir._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.generic.auto._

object CandidateEndpoints {
  val candidateBaseEndpoint = baseEndpoint.in("candidate").description("Candidate Endpoints: Provides all functionality to manage candidates catalogue data")
  // in(path[Int]("cid").and(jsonBody[Candidate]))
  val candidateCreateEndpoint: PublicEndpoint[Candidate, ErrorInfo, Candidate, Any] =
    candidateBaseEndpoint
      .post
      .description("Creates a new candidate")
      .in(jsonBody[Candidate]
        .description("Candidate's information to be persisted"))
      .out(jsonBody[Candidate]
        .description("Persisted candidates information"))
  //   ^-- TODO: probably we should return empty body with 204 - No Content,
  //    because we are not generating any new data on the server

  val candidateUpdateEndpoint =
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

  val candidateByID: PublicEndpoint[String, ErrorInfo, Candidate, Any] =
    candidateBaseEndpoint
      .get
      .description("Retrieves candidate's data by ID")
      .in(path[String]("cid")
        .description("Candidate Id"))
      .out(jsonBody[Candidate]
        .description("Candidate information. Will return 404 - Not Found if the candidate ID is not found"))

  val candidateListAll: PublicEndpoint[Unit, ErrorInfo, List[Candidate], Any] =
    candidateBaseEndpoint
      .get
      .description("Retrieves all candidates.")
      .out(jsonBody[List[Candidate]]
        .description("A list of candidates"))

  val allEndpoints = List(candidateCreateEndpoint, candidateUpdateEndpoint, candidateByID, candidateByID)

  val allServerEndpoints = List(
    candidateCreateEndpoint.serverLogic(CandidatesService.createCandidate _),
    candidateUpdateEndpoint.serverLogic((CandidatesService.updateCandidate _).tupled),
    candidateByID.serverLogic((CandidatesService.findCandidateById _)),
    candidateListAll.serverLogic(_ => CandidatesService.findAllCandidate())
  )
}
