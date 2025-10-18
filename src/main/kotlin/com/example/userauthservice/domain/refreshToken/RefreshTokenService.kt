package com.example.userauthservice.domain.refreshToken

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Transactional
    fun createRefreshToken(
        userId: Long,
        token: String,
        expiresAt: LocalDateTime,
    ): RefreshToken {
        val refreshToken =
            RefreshToken(
                userId = userId,
                token = token,
                expiresAt = expiresAt,
            )
        return refreshTokenRepository.save(refreshToken)
    }
}
