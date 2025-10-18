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
    val name: String = name

    @Column(unique = true, nullable = false)
    val email: String = email

    @Column(nullable = false)
    val password: String = password

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: Role = role

    init {
        require(this.email.isValidEmail()) { ErrorMessage.INVALID.INVALID_EMAIL_FORMAT.message }
    }
}

enum class Role {
    ADMIN,
    MEMBER,
}
