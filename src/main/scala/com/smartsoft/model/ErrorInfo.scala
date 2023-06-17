package com.smartsoft.model

import sttp.model.StatusCode

case class ErrorInfo(code: StatusCode, message: String)
