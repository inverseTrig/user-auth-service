package com.example.userauthservice

object ErrorMessage {
    interface Error {
        val message: String
    }

    enum class INVALID(
        override val message: String,
    ) : Error {
        EMAIL_ALREADY_EXISTS("Email already exists"),
        INVALID_EMAIL_FORMAT("Invalid email format"),
        INVALID_CREDENTIALS("Invalid email or password"),
        INVALID_TOKEN("Invalid or expired token"),
    }
}
