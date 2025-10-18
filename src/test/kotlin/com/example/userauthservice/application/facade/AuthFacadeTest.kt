package com.example.userauthservice.application.facade

import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.application.configuration.security.JwtTokenProcessor
import com.example.userauthservice.domain.refreshToken.RefreshToken
import com.example.userauthservice.domain.refreshToken.RefreshTokenService
import com.example.userauthservice.domain.user.Role
import com.example.userauthservice.domain.user.User
import com.example.userauthservice.domain.user.UserService
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
    }
}
