package com.smartsoft.security

import com.smartsoft.model.User
import com.typesafe.config.Config
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.{Claims, Jwts}

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.{Instant, LocalDate}
import java.util.{Date, UUID}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class JwtService(config: Config)(implicit val ec: ExecutionContext) {
  private val secret: String = config.getString("jwt.secret")
  private val jwtTTL: Long = config.getLong("jwt.expiration.seconds")

  def getUser(token: String): Future[Either[Throwable, Option[User]]] = {
    val urlDecodedToken: String = URLDecoder.decode(token, StandardCharsets.UTF_8.toString)
    Try {
      Jwts
        .parserBuilder()
        .setSigningKey(secret.getBytes(StandardCharsets.UTF_8.toString))
        .build().parseClaimsJws(urlDecodedToken)
    } match {
      case Failure(exception) => Future.successful(Left(exception))
      case Success(claims) =>
        val jwtClaims: Claims = claims.getBody()
        Option(jwtClaims.get("userId").toString) match {
          case Some(userId) if userId == "rockamorales" =>
            Future.successful(Right(Option(User("rockamorales", "Roberto", "Morales", "rockamorales@hotmail.com", "some-password-hash", LocalDate.now()))))
          case Some(userId) => Future.successful(Right(None))
          case None => Future.successful(Right(None))

        }

    }
  }

  def generateToken(userId: String): String = {
    val now = Instant.now
    val jwt = Jwts
      .builder()
      .setId(UUID.randomUUID().toString)
      .setIssuedAt(Date.from(now))
      .setExpiration(Date.from(now.plusSeconds(jwtTTL.toInt)))
      .signWith(
        Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8.toString))
      ).claim("userId", userId) // adding claim

    jwt.compact()
  }
}
