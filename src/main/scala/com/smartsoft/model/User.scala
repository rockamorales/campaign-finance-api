package com.smartsoft.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.LocalDateTime

case class User(userCode: String, firstName: String,
                lastName: String, email: String,
                address: Option[String], password: String,
                created: Option[LocalDateTime] = null)

object User {
  lazy implicit val userDecoder: Decoder[User] = deriveDecoder[User]
  lazy implicit val userEncoder: Encoder[User] = deriveEncoder[User]
}