package com.example.userauthservice.application.presentation.dto.auth

import com.example.userauthservice.domain.user.CreateUserData
import com.example.userauthservice.domain.user.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateUserRequest(
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

data class UserResponse(
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
