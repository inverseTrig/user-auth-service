package com.example.userauthservice.application.facade

import com.example.userauthservice.application.configuration.security.JwtTokenProcessor
import com.example.userauthservice.domain.refreshToken.RefreshTokenService
import com.example.userauthservice.domain.user.User
import com.example.userauthservice.domain.user.UserService
import org.springframework.stereotype.Component

@Component
class AuthFacade(
    private val userService: UserService,
    private val refreshTokenService: RefreshTokenService,
    private val jwtTokenProcessor: JwtTokenProcessor,
) {
    fun authenticate(
        email: String,
        password: String,
    ): AuthenticateResult {
        val user = userService.authenticate(email, password)

        val accessToken = jwtTokenProcessor.generateAccessToken(user)
        val refreshToken = jwtTokenProcessor.generateRefreshToken(user)

        refreshTokenService.createRefreshToken(
            userId = user.id,
            token = refreshToken,
            expiresAt = jwtTokenProcessor.getExpirationDate(refreshToken),
        )

        return AuthenticateResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtTokenProcessor.accessTokenExpiration,
            user = user,
        )
    }
}

data class AuthenticateResult(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: User,
)
