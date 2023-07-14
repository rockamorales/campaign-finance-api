package com.smartsoft.security

import com.smartsoft.exceptions.TokenExpiredException
import com.smartsoft.model.User
import com.typesafe.config.Config
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.{Claims, Jws, Jwts}

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.{Instant, LocalDate, LocalDateTime}
import java.util.{Date, UUID}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class JwtService(config: Config)(implicit val ec: ExecutionContext) {
  private val secret: String = config.getString("jwt.secret")
  private val jwtTTL: Long = config.getLong("jwt.expiration.seconds")

  def getUserCode(claims: Jws[Claims]): Option[String] = {
    val jwtClaims: Claims = claims.getBody()
    Option(jwtClaims.get("userCode").toString)
  }

  def getSessionId(claims: Jws[Claims]): Option[String] = {
    val jwtClaims: Claims = claims.getBody()
    Option(jwtClaims.get("sessionId").toString)
  }

  def validateToken(token: String): Try[Jws[Claims]] = {
    val urlDecodedToken: String = URLDecoder.decode(token, StandardCharsets.UTF_8.toString)
      Try {
        Jwts
          .parserBuilder()
          .setSigningKey(secret.getBytes(StandardCharsets.UTF_8.toString))
          .build().parseClaimsJws(urlDecodedToken)
      } match {
        case Failure(exception) => Failure(exception)
        case Success(claims) =>
          val jwtClaims: Claims = claims.getBody()
          if (new Date().before(jwtClaims.getExpiration)) {
            Failure (new TokenExpiredException)
          }
          Success(claims)
      }
  }

  def getUserCode(token: String): Try[Option[String]] = {
    val urlDecodedToken: String = URLDecoder.decode(token, StandardCharsets.UTF_8.toString)
    Try {
      Jwts
        .parserBuilder()
        .setSigningKey(secret.getBytes(StandardCharsets.UTF_8.toString))
        .build().parseClaimsJws(urlDecodedToken)
    } match {
      case Failure(exception) => Failure(exception)
      case Success(claims) =>
        val jwtClaims: Claims = claims.getBody()
        Success(Option(jwtClaims.get("userCode")).map(userCode => userCode.toString))
    }
  }


  def generateToken(userCode: String, sessionId: String): String = {
    val now = Instant.now
    val jwt = Jwts
      .builder()
      .setId(UUID.randomUUID().toString)
      .setIssuedAt(Date.from(now))
      .setExpiration(Date.from(now.plusSeconds(jwtTTL.toInt)))
      .signWith(
        Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8.toString))
      )
      .claim("userCode", userCode)
      .claim("sessionId", sessionId)
      // adding claim

    jwt.compact()
  }
}
