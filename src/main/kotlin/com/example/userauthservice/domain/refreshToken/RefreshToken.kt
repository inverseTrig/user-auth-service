@file:Suppress("CanBePrimaryConstructorProperty")

package com.example.userauthservice.domain.refreshToken

import com.example.userauthservice.domain.BaseEntity
import com.example.userauthservice.domain.generateId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    userId: Long,
    token: String,
    familyId: Long = generateId(),
    expiresAt: LocalDateTime,
) : BaseEntity() {
    @Column(nullable = false)
    val userId: Long = userId

    @Column(unique = true, nullable = false, length = 512)
    val token: String = token

    @Column(nullable = false)
    val familyId: Long = familyId

    @Column(nullable = false)
    var isRevoked: Boolean = false
        private set

    @Column(nullable = false)
    val expiresAt: LocalDateTime = expiresAt

    fun revoke() {
        this.isRevoked = true
    }
}
