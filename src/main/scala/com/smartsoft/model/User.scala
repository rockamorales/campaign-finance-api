package com.smartsoft.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.LocalDateTime

case class User(userCode: String, firstName: String,
                lastName: String, email: String,
                address: String, password: String,
                created: LocalDateTime = null)

object User {
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
}