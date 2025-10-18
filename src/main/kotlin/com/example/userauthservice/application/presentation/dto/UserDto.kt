@file:Suppress("ktlint:standard:filename")

package com.example.userauthservice.application.presentation.dto

import com.example.userauthservice.domain.user.UpdateUserServiceData
import com.example.userauthservice.domain.user.User
import jakarta.validation.constraints.Email
import java.time.LocalDateTime

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

data class UpdateUserRequest(
    val name: String?,
    @field:Email(message = "Invalid email format")
    val email: String?,
) {
    fun toData(id: Long): UpdateUserServiceData {
        return UpdateUserServiceData(
            id = id,
            name = this.name,
            email = this.email,
        )
    }
}
