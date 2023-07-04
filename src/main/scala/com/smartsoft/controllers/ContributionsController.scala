package com.smartsoft.controllers

import com.smartsoft.security.APISecurity
import com.smartsoft.services.CandidatesService

import scala.concurrent.ExecutionContext

class ContributionsController (apiSecurity: APISecurity, candidatesService: CandidatesService)
                              (implicit ec: ExecutionContext) extends APIController {

}
