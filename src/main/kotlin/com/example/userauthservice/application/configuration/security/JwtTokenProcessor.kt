package com.example.userauthservice.application.configuration.security

import com.example.userauthservice.ErrorMessage
import com.example.userauthservice.domain.generateId
import com.example.userauthservice.domain.user.Role
import com.example.userauthservice.domain.user.User
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProcessor(
    private val jwtProperties: JwtProperties,
) {
    private val logger = KotlinLogging.logger {}

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    val accessTokenExpiration: Long = jwtProperties.accessTokenExpiration

    fun generateAccessToken(user: User): String =
        generateToken(
            userId = user.id,
            email = user.email,
            role = user.role,
            expiration = jwtProperties.accessTokenExpiration,
            tokenType = TokenType.ACCESS.name,
        )

    fun generateRefreshToken(user: User): String =
        generateToken(
            userId = user.id,
            email = user.email,
            role = user.role,
            expiration = jwtProperties.refreshTokenExpiration,
            tokenType = TokenType.REFRESH.name,
        )

    private fun generateToken(
        userId: Long,
        email: String,
        role: Role,
        expiration: Long,
        tokenType: String,
    ): String {
        val zone = ZoneId.systemDefault()

        val now = LocalDateTime.now()
        val expiryDateTime = now.plusSeconds(expiration / 1000)

        val nowDate = Date.from(now.atZone(zone).toInstant())
        val expiryDate = Date.from(expiryDateTime.atZone(zone).toInstant())

        return Jwts
            .builder()
            .subject(userId.toString())
            .id(generateId().toString())
            .claim("email", email)
            .claim("role", role.name)
            .claim("type", tokenType)
            .issuer(jwtProperties.issuer)
            .issuedAt(nowDate)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun getExpirationDate(token: String): LocalDateTime {
        val claims = getClaims(token)
        return claims.expiration.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    fun getPrincipal(token: String): TokenPrincipal {
        val claims = getClaims(token)
        return TokenPrincipal(
            userId = claims.subject.toLong(),
            email = claims["email"] as String,
            role = claims["role"] as String,
            tokenType = claims["type"] as String,
            issuedAt =
                claims.issuedAt.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime(),
            expiresAt =
                claims.expiration.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime(),
        )
    }

    private fun getClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: SignatureException) {
            logger.error(e) { "Invalid JWT signature: ${e.message}" }
            throw IllegalArgumentException(ErrorMessage.INVALID.INVALID_TOKEN.message, e)
        } catch (e: MalformedJwtException) {
            logger.error(e) { "Invalid JWT token: ${e.message}" }
            throw IllegalArgumentException(ErrorMessage.INVALID.INVALID_TOKEN.message, e)
        } catch (e: ExpiredJwtException) {
            logger.error(e) { "Expired JWT token: ${e.message}" }
            throw IllegalArgumentException(ErrorMessage.INVALID.INVALID_TOKEN.message, e)
        } catch (e: UnsupportedJwtException) {
            logger.error(e) { "Unsupported JWT token: ${e.message}" }
            throw IllegalArgumentException(ErrorMessage.INVALID.INVALID_TOKEN.message, e)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "JWT claims string is empty: ${e.message}" }
            throw IllegalArgumentException(ErrorMessage.INVALID.INVALID_TOKEN.message, e)
        }
    }
}

enum class TokenType {
    ACCESS,
    REFRESH,
}

data class TokenPrincipal(
    val userId: Long,
    val email: String,
    val role: String,
    val tokenType: String,
    val issuedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
)
