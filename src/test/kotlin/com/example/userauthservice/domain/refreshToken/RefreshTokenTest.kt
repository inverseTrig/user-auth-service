package com.example.userauthservice.domain.refreshToken

import com.example.userauthservice.UnitTestBase
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class RefreshTokenTest : UnitTestBase() {
    init {
        context("revoke") {
            test("리프레시 토큰을 무효화한다.") {
                // Given
                val refreshToken =
                    RefreshToken(
                        userId = 1L,
                        token = "test-token",
                        expiresAt = LocalDateTime.of(2025, 12, 31, 23, 59, 59),
                    )

                // When
                refreshToken.revoke()

                // Then
                refreshToken.isRevoked shouldBe true
            }
        }
    }
}
