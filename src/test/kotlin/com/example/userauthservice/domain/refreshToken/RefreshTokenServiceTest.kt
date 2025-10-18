package com.example.userauthservice.domain.refreshToken

import com.example.userauthservice.UnitTestBase
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

class RefreshTokenServiceTest : UnitTestBase() {
    private val refreshTokenRepository: RefreshTokenRepository = mockk()

    private val refreshTokenService: RefreshTokenService =
        RefreshTokenService(
            refreshTokenRepository = refreshTokenRepository,
        )

    init {
        context("createRefreshToken") {
            test("주어진 정보로 리프레시 토큰을 생성하고 저장한다.") {
                // Given
                val userId = 1L
                val token = "test-refresh-token"
                val expiresAt = LocalDateTime.of(2025, 12, 31, 23, 59, 59)

                every { refreshTokenRepository.save(any()) } returnsArgument 0

                // When
                val result = refreshTokenService.createRefreshToken(userId, token, expiresAt)

                // Then
                assertSoftly(result) {
                    it.userId shouldBe userId
                    it.token shouldBe token
                    it.expiresAt shouldBe expiresAt
                    it.isRevoked shouldBe false
                }

                verify {
                    val matcher =
                        withArg<RefreshToken> {
                            it.userId shouldBe userId
                            it.token shouldBe token
                            it.expiresAt shouldBe expiresAt
                            it.isRevoked shouldBe false
                        }
                    refreshTokenRepository.save(matcher)
                }
            }
        }
    }
}
