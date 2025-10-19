package com.example.userauthservice.domain.refreshToken

import com.example.userauthservice.ErrorMessage
import com.example.userauthservice.InvalidTokenException
import com.example.userauthservice.domain.generateId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val refreshTokenSecurityService: RefreshTokenSecurityService,
) {
    @Transactional
    fun createRefreshToken(
        userId: Long,
        token: String,
        expiresAt: LocalDateTime,
        familyId: Long? = null,
    ): RefreshToken {
        val refreshToken =
            RefreshToken(
                userId = userId,
                token = token,
                expiresAt = expiresAt,
                familyId = familyId ?: generateId(),
            )
        return refreshTokenRepository.save(refreshToken)
    }

    @Transactional
    fun rotateRefreshToken(oldToken: String): RefreshTokenRotationResult {
        val oldRefreshToken =
            refreshTokenRepository.findByToken(oldToken)
                .orElseThrow { InvalidTokenException(ErrorMessage.INVALID.INVALID_TOKEN.message) }

        if (oldRefreshToken.isRevoked) {
            refreshTokenSecurityService.revokeTokenFamily(oldRefreshToken.familyId)
            throw InvalidTokenException(ErrorMessage.INVALID.INVALID_TOKEN.message)
        }

        if (oldRefreshToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw InvalidTokenException(ErrorMessage.INVALID.INVALID_TOKEN.message)
        }

        oldRefreshToken.revoke()

        return RefreshTokenRotationResult(
            userId = oldRefreshToken.userId,
            familyId = oldRefreshToken.familyId,
        )
    }
}

data class RefreshTokenRotationResult(
    val userId: Long,
    val familyId: Long,
)
