package com.smartsoft.security

import org.mindrot.jbcrypt.BCrypt

import scala.util.Try

class EncryptionService {
  def encrypt(text: String): String = {
    BCrypt.hashpw(text, BCrypt.gensalt())
  }

  def checkPassword(providedPassword: String, storedPassword: String): Unit = {
    Try {
      BCrypt.checkpw(providedPassword, storedPassword)
    }
  }
}
