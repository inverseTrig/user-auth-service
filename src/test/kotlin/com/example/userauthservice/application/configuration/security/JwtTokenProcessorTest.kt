package com.example.userauthservice.application.configuration.security

import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.domain.user.Role
import com.example.userauthservice.domain.user.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import java.util.Date

class JwtTokenProcessorTest : UnitTestBase() {
    private val jwtProperties =
        JwtProperties(
            secret = "test-secret-key-that-is-at-least-256-bits-long-for-hs256",
            accessTokenExpiration = 3600000L,
            refreshTokenExpiration = 604800000L,
            issuer = "test-issuer",
        )
    private val jwtTokenProcessor = JwtTokenProcessor(jwtProperties)

    init {
        context("generateAccessToken") {
            test("올바른 클레임을 가진 유효한 액세스 토큰을 생성한다") {
                // Given
                val user =
                    User(
                        name = "Test User",
                        email = "test@example.com",
                        password = "encryptedPassword",
                        role = Role.MEMBER,
                    )

                // When
                val token = jwtTokenProcessor.generateAccessToken(user)

                // Then
                token.shouldNotBeEmpty()

                val actualPrincipal = jwtTokenProcessor.getPrincipal(token)
                assertSoftly(actualPrincipal) {
                    it.userId shouldBe user.id
                    it.email shouldBe user.email
                    it.role shouldBe user.role.name
                    it.tokenType shouldBe TokenType.ACCESS.name
                    it.expiresAt shouldBe it.issuedAt.plusSeconds(jwtProperties.accessTokenExpiration / 1000)
                }
            }
        }

        context("generateRefreshToken") {
            test("올바른 클레임을 가진 유효한 리프레시 토큰을 생성한다") {
                // Given
                val user =
                    User(
                        name = "Test User",
                        email = "test@example.com",
                        password = "encryptedPassword",
                        role = Role.MEMBER,
                    )

                // When
                val token = jwtTokenProcessor.generateRefreshToken(user)

                // Then
                token.shouldNotBeEmpty()

                val actualPrincipal = jwtTokenProcessor.getPrincipal(token)
                assertSoftly(actualPrincipal) {
                    it.userId shouldBe user.id
                    it.email shouldBe user.email
                    it.role shouldBe user.role.name
                    it.tokenType shouldBe TokenType.REFRESH.name
                    it.expiresAt shouldBe it.issuedAt.plusSeconds(jwtProperties.refreshTokenExpiration / 1000)
                }
            }
        }

        context("getExpirationDate") {
            test("유효한 토큰의 만료 날짜를 반환한다") {
                // Given
                val user =
                    User(
                        name = "Test User",
                        email = "test@example.com",
                        password = "encryptedPassword",
                        role = Role.MEMBER,
                    )
                val token = jwtTokenProcessor.generateAccessToken(user)

                // When
                val expirationDate = jwtTokenProcessor.getExpirationDate(token)

                // Then
                val actualPrincipal = jwtTokenProcessor.getPrincipal(token)
                expirationDate shouldBe actualPrincipal.expiresAt
            }

            test("유효하지 않은 토큰에 대해 예외를 던진다") {
                // Given
                val invalidToken = "invalid.token.string"

                // Expect
                shouldThrow<IllegalArgumentException> {
                    jwtTokenProcessor.getExpirationDate(invalidToken)
                }
            }

            test("만료된 토큰에 대해 예외를 던진다") {
                // Given
                val secretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
                val expiredToken =
                    Jwts.builder()
                        .subject("1")
                        .claim("email", "test@example.com")
                        .claim("role", Role.MEMBER.name)
                        .claim("type", TokenType.ACCESS.name)
                        .issuer(jwtProperties.issuer)
                        .issuedAt(Date(System.currentTimeMillis() - 7200000L)) // 2 hours ago
                        .expiration(Date(System.currentTimeMillis() - 3600000L)) // 1 hour ago (expired)
                        .signWith(secretKey)
                        .compact()

                // Expect
                shouldThrow<IllegalArgumentException> {
                    jwtTokenProcessor.getExpirationDate(expiredToken)
                }
            }
        }

        context("getPrincipal") {
            test("유효한 토큰에서 올바른 principal 정보를 추출한다") {
                // Given
                val user =
                    User(
                        name = "Test User",
                        email = "test@example.com",
                        password = "encryptedPassword",
                        role = Role.ADMIN,
                    )
                val token = jwtTokenProcessor.generateAccessToken(user)

                // When
                val principal = jwtTokenProcessor.getPrincipal(token)

                // Then
                assertSoftly(principal) {
                    it.userId shouldBe user.id
                    it.email shouldBe user.email
                    it.role shouldBe user.role.name
                    it.tokenType shouldBe TokenType.ACCESS.name
                    it.expiresAt shouldBe it.issuedAt.plusSeconds(jwtProperties.accessTokenExpiration / 1000)
                }
            }

            test("리프레시 토큰에서 올바른 토큰 타입을 추출한다") {
                // Given
                val user =
                    User(
                        name = "Test User",
                        email = "test@example.com",
                        password = "encryptedPassword",
                        role = Role.MEMBER,
                    )
                val token = jwtTokenProcessor.generateRefreshToken(user)

                // When
                val principal = jwtTokenProcessor.getPrincipal(token)

                // Then
                principal.tokenType shouldBe TokenType.REFRESH.name
            }

            test("잘못된 형식의 토큰에 대해 예외를 던진다") {
                // Given
                val malformedToken = "this.is.malformed"

                // Expect
                shouldThrow<IllegalArgumentException> {
                    jwtTokenProcessor.getPrincipal(malformedToken)
                }
            }

            test("유효하지 않은 서명을 가진 토큰에 대해 예외를 던진다") {
                // Given
                val differentSecretKey =
                    Keys.hmacShaKeyFor("different-secret-key-that-is-at-least-256-bits".toByteArray())
                val tokenWithWrongSignature =
                    Jwts.builder()
                        .subject("1")
                        .claim("email", "test@example.com")
                        .claim("role", Role.MEMBER.name)
                        .claim("type", TokenType.ACCESS.name)
                        .issuer(jwtProperties.issuer)
                        .issuedAt(Date())
                        .expiration(Date(System.currentTimeMillis() + 3600000L))
                        .signWith(differentSecretKey)
                        .compact()

                // Expect
                shouldThrow<IllegalArgumentException> {
                    jwtTokenProcessor.getPrincipal(tokenWithWrongSignature)
                }
            }

            test("만료된 토큰에 대해 예외를 던진다") {
                // Given
                val secretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
                val expiredToken =
                    Jwts.builder()
                        .subject("1")
                        .claim("email", "test@example.com")
                        .claim("role", Role.MEMBER.name)
                        .claim("type", TokenType.ACCESS.name)
                        .issuer(jwtProperties.issuer)
                        .issuedAt(Date(System.currentTimeMillis() - 7200000L))
                        .expiration(Date(System.currentTimeMillis() - 3600000L))
                        .signWith(secretKey)
                        .compact()

                // Expect
                shouldThrow<IllegalArgumentException> {
                    jwtTokenProcessor.getPrincipal(expiredToken)
                }
            }
        }
    }
}
