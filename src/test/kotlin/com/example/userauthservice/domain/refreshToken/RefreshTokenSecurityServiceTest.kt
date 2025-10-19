package com.example.userauthservice.domain.refreshToken

import com.example.userauthservice.UnitTestBase
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

class RefreshTokenSecurityServiceTest : UnitTestBase() {
    private val refreshTokenRepository: RefreshTokenRepository = mockk()

    private val refreshTokenSecurityService: RefreshTokenSecurityService =
        RefreshTokenSecurityService(
            refreshTokenRepository = refreshTokenRepository,
        )

    init {
        context("revokeTokenFamily") {
            test("주어진 familyId에 속한 모든 리프레시 토큰을 무효화한다.") {
                // Given
                val familyId = 100L
                val token1 =
                    RefreshToken(
                        userId = 1L,
                        token = "token-1",
                        familyId = familyId,
                        expiresAt = LocalDateTime.of(2025, 12, 31, 23, 59, 59),
                    )
                val token2 =
                    RefreshToken(
                        userId = 1L,
                        token = "token-2",
                        familyId = familyId,
                        expiresAt = LocalDateTime.of(2025, 12, 31, 23, 59, 59),
                    )
                val token3 =
                    RefreshToken(
                        userId = 1L,
                        token = "token-3",
                        familyId = familyId,
                        expiresAt = LocalDateTime.of(2025, 12, 31, 23, 59, 59),
                    )

                val tokensInFamily = listOf(token1, token2, token3)

                every { refreshTokenRepository.findAllByFamilyId(familyId) } returns tokensInFamily

                // When
                refreshTokenSecurityService.revokeTokenFamily(familyId)

                // Then
                token1.isRevoked shouldBe true
                token2.isRevoked shouldBe true
                token3.isRevoked shouldBe true
            }
        }
    }
}
