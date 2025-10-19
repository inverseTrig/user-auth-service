package com.example.userauthservice.domain.refreshToken

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): Optional<RefreshToken>

    fun findAllByFamilyId(familyId: Long): List<RefreshToken>
}
