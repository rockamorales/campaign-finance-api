package com.smartsoft.services

import com.smartsoft.model.{Candidate, ErrorInfo}
import sttp.model.StatusCode

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class CandidatesService() {
  def findAllCandidate(): Either[ErrorInfo, List[Candidate]] = {
    Right(List(Candidate("","","","","","")))
  }

  def findCandidateById(cid: String): Either[ErrorInfo, Candidate] = {
    Right(Candidate("","","","","",""))
  }

  def createCandidate(candidate: Candidate): Either[ErrorInfo, Candidate] = {
    Right(Candidate("", "", "", "", "", ""))
  }

  def updateCandidate(cid: String, candidate: Candidate): Either[ErrorInfo, StatusCode] = {
    // update logic
    Right(StatusCode.NoContent)
  }
}
