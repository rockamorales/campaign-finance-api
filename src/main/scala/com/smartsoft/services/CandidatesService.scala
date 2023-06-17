package com.smartsoft.services

import com.smartsoft.model.{Candidate, ErrorInfo}
import sttp.model.StatusCode

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class CandidatesService() {
  def findAllCandidate(): Future[Either[ErrorInfo, List[Candidate]]] = {
    Future(Right(List(Candidate("","","","","",""))))
  }

  def findCandidateById(cid: String): Future[Either[ErrorInfo, Candidate]] = {
    Future(Right(Candidate("","","","","","")))
  }

  def createCandidate(candidate: Candidate): Future[Either[ErrorInfo, Candidate]] = {
    Future(Right(Candidate("", "", "", "", "", "")))
  }

  def updateCandidate(cid: String, candidate: Candidate): Future[Either[ErrorInfo, StatusCode]] = {
    // update logic
    Future(Right(StatusCode.NoContent))
  }
}
