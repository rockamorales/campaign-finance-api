package com.smartsoft.exceptions

case class TokenExpiredException() extends RuntimeException("Token is expired")
