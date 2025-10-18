@file:Suppress("CanBePrimaryConstructorProperty")

package com.example.userauthservice.domain.user

import com.example.userauthservice.ErrorMessage
import com.example.userauthservice.domain.BaseEntity
import com.example.userauthservice.isValidEmail
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    name: String,
    email: String,
    password: String,
    role: Role,
) : BaseEntity() {
    @Column(nullable = false)
    var name: String = name
        private set

    @Column(unique = true, nullable = false)
    var email: String = email
        private set(value) {
            require(value.isValidEmail()) { ErrorMessage.INVALID.INVALID_EMAIL_FORMAT.message }
            field = value
        }

    @Column(nullable = false)
    var password: String = password
        private set

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: Role = role

    init {
        this.email = email
    }

    fun update(data: UpdateUserData) {
        data.email?.let { this.email = it }
        data.name?.let { this.name = it }
    }
}

enum class Role {
    ADMIN,
    MEMBER,
}

data class UpdateUserData(
    val name: String?,
    val email: String?,
)
