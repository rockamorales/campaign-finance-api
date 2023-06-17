package com.smartsoft.model

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.LocalDate

case class User(id: String, firstName: String, lastName: String, email: String, passwordHash: String, created: LocalDate)

object User {
  implicit val userDecoder = deriveDecoder[User]
  implicit val userEncoder = deriveEncoder[User]
}