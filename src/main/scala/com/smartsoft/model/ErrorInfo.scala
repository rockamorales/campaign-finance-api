package com.smartsoft.model

import sttp.model.StatusCode
sealed trait ErrorInfo
abstract class HttpErrorInfo(val code: Int) extends ErrorInfo
case class Unauthorized(message: String) extends HttpErrorInfo(StatusCode.Unauthorized.code)
case class NotFound(message: String) extends HttpErrorInfo(StatusCode.NotFound.code)
case class NoContent(message: String) extends HttpErrorInfo(StatusCode.NoContent.code)
case class NotImplemented(message: String) extends HttpErrorInfo(StatusCode.NotImplemented.code)
case class InternalServerError(message: String) extends HttpErrorInfo(StatusCode.InternalServerError.code)
case class BadRequest(message: String) extends HttpErrorInfo(StatusCode.BadRequest.code)
case class Forbidden(message: String) extends HttpErrorInfo(StatusCode.Forbidden.code)
case class Ok(message: String) extends HttpErrorInfo(StatusCode.Ok.code)
case class Created(message: String) extends HttpErrorInfo(StatusCode.Created.code)
