package com.example.userauthservice.domain.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): User?

    fun existsByEmailAndIdNot(
        email: String,
        id: Long,
    ): Boolean
}
