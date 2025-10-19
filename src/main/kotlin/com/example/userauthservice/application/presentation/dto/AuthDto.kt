package com.example.userauthservice.application.presentation.dto

import com.example.userauthservice.application.facade.AuthenticationResult
import com.example.userauthservice.domain.user.CreateUserData
import com.example.userauthservice.domain.user.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class SignUpRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    val password: String,
) {
    fun toData() =
        CreateUserData(
            name = this.name,
            email = this.email,
            password = this.password,
        )
}

data class SignUpResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    constructor(user: User) : this(
        id = user.id,
        name = user.name,
        email = user.email,
        role = user.role.name,
        createdAt = user.createdAt,
        updatedAt = user.updatedAt,
    )
}

data class SignInRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:NotBlank(message = "Password is required")
    val password: String,
)

data class UserTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserInfo,
) {
    data class UserInfo(
        val id: Long,
        val email: String,
        val name: String,
        val role: String,
    )

    constructor(
        result: AuthenticationResult,
    ) : this(
        accessToken = result.accessToken,
        refreshToken = result.refreshToken,
        expiresIn = result.expiresIn,
        user =
            UserInfo(
                id = result.user.id,
                email = result.user.email,
                name = result.user.name,
                role = result.user.role.name,
            ),
    )
}

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String,
)
