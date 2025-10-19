package com.example.userauthservice.application.facade

import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.application.configuration.security.JwtTokenProcessor
import com.example.userauthservice.domain.refreshToken.RefreshToken
import com.example.userauthservice.domain.refreshToken.RefreshTokenRotationResult
import com.example.userauthservice.domain.refreshToken.RefreshTokenService
import com.example.userauthservice.domain.user.Role
import com.example.userauthservice.domain.user.User
import com.example.userauthservice.domain.user.UserService
import com.example.userauthservice.generateEmail
import com.example.userauthservice.generateString
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

class AuthFacadeTest : UnitTestBase() {
    private val userService: UserService = mockk()
    private val refreshTokenService: RefreshTokenService = mockk()
    private val jwtTokenProcessor: JwtTokenProcessor = mockk()

    private val authFacade: AuthFacade =
        AuthFacade(
            userService = userService,
            refreshTokenService = refreshTokenService,
            jwtTokenProcessor = jwtTokenProcessor,
        )

    init {
        context("authenticate") {
            test("유효한 자격 증명으로 인증하고 토큰을 생성한다.") {
                // Given
                val email = "test@email.com"
                val password = "securePassword"
                val accessToken = "access-token"
                val refreshToken = "refresh-token"
                val expiresIn = 3600000L
                val expiresAt = LocalDateTime.of(2025, 12, 31, 23, 59, 59)

                val user =
                    User(
                        name = "testName",
                        email = email,
                        password = "encryptedPassword",
                        role = Role.MEMBER,
                    )

                val savedRefreshToken =
                    RefreshToken(
                        userId = user.id,
                        token = refreshToken,
                        expiresAt = expiresAt,
                    )

                every { userService.authenticate(email, password) } returns user
                every { jwtTokenProcessor.generateAccessToken(user) } returns accessToken
                every { jwtTokenProcessor.generateRefreshToken(user) } returns refreshToken
                every { jwtTokenProcessor.getExpirationDate(refreshToken) } returns expiresAt
                every { jwtTokenProcessor.accessTokenExpiration } returns expiresIn
                every {
                    refreshTokenService.createRefreshToken(
                        user.id,
                        refreshToken,
                        expiresAt,
                    )
                } returns savedRefreshToken

                // When
                val result = authFacade.authenticate(email, password)

                // Then
                assertSoftly(result) {
                    it.accessToken shouldBe accessToken
                    it.refreshToken shouldBe refreshToken
                    it.expiresIn shouldBe expiresIn
                    it.user shouldBe user
                }

                verify {
                    userService.authenticate(email, password)
                    jwtTokenProcessor.generateAccessToken(user)
                    jwtTokenProcessor.generateRefreshToken(user)
                    jwtTokenProcessor.getExpirationDate(refreshToken)
                    refreshTokenService.createRefreshToken(user.id, refreshToken, expiresAt)
                }
            }
        }

        context("refreshToken") {
            test("유효한 리프레시 토큰으로 새로운 토큰을 생성한다.") {
                // Given
                val oldRefreshToken = "old-refresh-token"
                val newAccessToken = "new-access-token"
                val newRefreshToken = "new-refresh-token"
                val expiresIn = 3600000L
                val expiresAt = LocalDateTime.of(2025, 12, 31, 23, 59, 59)
                val familyId = 100L

                val user =
                    User(
                        name = generateString(),
                        email = generateEmail(),
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val rotationResult =
                    RefreshTokenRotationResult(
                        userId = user.id,
                        familyId = familyId,
                    )

                val savedRefreshToken =
                    RefreshToken(
                        userId = user.id,
                        token = newRefreshToken,
                        familyId = familyId,
                        expiresAt = expiresAt,
                    )

                every { refreshTokenService.rotateRefreshToken(oldRefreshToken) } returns rotationResult
                every { userService.getById(user.id) } returns user
                every { jwtTokenProcessor.generateAccessToken(user) } returns newAccessToken
                every { jwtTokenProcessor.generateRefreshToken(user) } returns newRefreshToken
                every { jwtTokenProcessor.getExpirationDate(newRefreshToken) } returns expiresAt
                every { jwtTokenProcessor.accessTokenExpiration } returns expiresIn
                every {
                    refreshTokenService.createRefreshToken(
                        userId = user.id,
                        token = newRefreshToken,
                        expiresAt = expiresAt,
                        familyId = familyId,
                    )
                } returns savedRefreshToken

                // When
                val result = authFacade.refreshToken(oldRefreshToken)

                // Then
                assertSoftly(result) {
                    it.accessToken shouldBe newAccessToken
                    it.refreshToken shouldBe newRefreshToken
                    it.expiresIn shouldBe expiresIn
                    it.user shouldBe user
                }

                verify {
                    refreshTokenService.rotateRefreshToken(oldRefreshToken)
                    refreshTokenService.createRefreshToken(user.id, newRefreshToken, expiresAt, familyId)
                }
            }
        }
    }
}
