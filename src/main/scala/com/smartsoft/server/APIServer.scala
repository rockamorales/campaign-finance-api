package com.smartsoft.server

trait APIServer[F[_]] {
  def start: F[_]
}