package com.smartsoft.model

import com.smartsoft.actors.SessionStates
import com.smartsoft.actors.SessionStates.SessionStates
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime

case class UserSessionDetails(sessionId: String, userCode: String,
                              startDate: LocalDateTime, endDate: Option[LocalDateTime],
                              state: SessionStates, message: String)

object UserSessionDetails {
  lazy implicit val userSessionDetailsDecoder: Decoder[UserSessionDetails] = deriveDecoder[UserSessionDetails]
  lazy implicit val sessionStateDecoder: Decoder[SessionStates.Value] = Decoder.decodeEnumeration(SessionStates)
  lazy implicit val sessionStateEncoder: Encoder[SessionStates.Value] = Encoder.encodeEnumeration(SessionStates)
  lazy implicit val userSessionDetailsEncoder: Encoder[UserSessionDetails] = deriveEncoder[UserSessionDetails]
}
