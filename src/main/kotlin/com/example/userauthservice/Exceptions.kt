@file:Suppress("ktlint:standard:filename")

package com.example.userauthservice

class InvalidCredentialsException(message: String) : RuntimeException(message)

class InvalidTokenException(message: String) : RuntimeException(message)
