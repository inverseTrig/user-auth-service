package com.example.userauthservice.domain.refreshToken

import com.example.userauthservice.ErrorMessage
import com.example.userauthservice.InvalidTokenException
import com.example.userauthservice.UnitTestBase
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.Optional

class RefreshTokenServiceTest : UnitTestBase() {
    private val refreshTokenRepository: RefreshTokenRepository = mockk()
    private val refreshTokenSecurityService: RefreshTokenSecurityService = mockk()

    private val refreshTokenService: RefreshTokenService =
        RefreshTokenService(
            refreshTokenRepository = refreshTokenRepository,
            refreshTokenSecurityService = refreshTokenSecurityService,
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

        context("rotateRefreshToken") {
            test("유효한 토큰으로 새로운 토큰 정보를 생성한다.") {
                // Given
                val oldToken = "old-refresh-token"
                val userId = 1L
                val familyId = 100L
                val expiresAt = LocalDateTime.now().plusDays(7)

                val oldRefreshToken =
                    RefreshToken(
                        userId = userId,
                        token = oldToken,
                        familyId = familyId,
                        expiresAt = expiresAt,
                    )

                every { refreshTokenRepository.findByToken(oldToken) } returns Optional.of(oldRefreshToken)

                // When
                val result = refreshTokenService.rotateRefreshToken(oldToken)

                // Then
                assertSoftly(result) {
                    it.userId shouldBe userId
                    it.familyId shouldBe familyId
                }

                oldRefreshToken.isRevoked shouldBe true
            }

            test("존재하지 않는 토큰으로 요청하면 예외를 발생시킨다.") {
                // Given
                val oldToken = "non-existent-token"

                every { refreshTokenRepository.findByToken(oldToken) } returns Optional.empty()

                // Expect
                val actual =
                    shouldThrow<InvalidTokenException> {
                        refreshTokenService.rotateRefreshToken(oldToken)
                    }
                actual.message shouldBe ErrorMessage.INVALID.INVALID_TOKEN.message
            }

            test("이미 무효화된 토큰으로 요청하면 토큰 패밀리를 무효화하고 예외를 발생시킨다.") {
                // Given
                val oldToken = "revoked-token"
                val userId = 1L
                val familyId = 100L
                val expiresAt = LocalDateTime.now().plusDays(7)

                val oldRefreshToken =
                    RefreshToken(
                        userId = userId,
                        token = oldToken,
                        familyId = familyId,
                        expiresAt = expiresAt,
                    ).apply { revoke() }

                every { refreshTokenRepository.findByToken(oldToken) } returns Optional.of(oldRefreshToken)

                // Expect
                val actual =
                    shouldThrow<InvalidTokenException> {
                        refreshTokenService.rotateRefreshToken(oldToken)
                    }
                actual.message shouldBe ErrorMessage.INVALID.INVALID_TOKEN.message

                verify { refreshTokenSecurityService.revokeTokenFamily(familyId) }
            }

            test("만료된 토큰으로 요청하면 예외를 발생시킨다.") {
                // Given
                val oldToken = "expired-token"
                val userId = 1L
                val familyId = 100L
                val expiresAt = LocalDateTime.now().minusDays(1)

                val oldRefreshToken =
                    RefreshToken(
                        userId = userId,
                        token = oldToken,
                        familyId = familyId,
                        expiresAt = expiresAt,
                    )

                every { refreshTokenRepository.findByToken(oldToken) } returns Optional.of(oldRefreshToken)

                // Expect
                val actual =
                    shouldThrow<InvalidTokenException> {
                        refreshTokenService.rotateRefreshToken(oldToken)
                    }
                actual.message shouldBe ErrorMessage.INVALID.INVALID_TOKEN.message
            }
        }
    }
}
