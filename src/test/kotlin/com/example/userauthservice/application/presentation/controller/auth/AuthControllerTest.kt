package com.example.userauthservice.application.presentation.controller.auth

import com.example.userauthservice.FunctionalTestBase
import com.example.userauthservice.application.presentation.ErrorResponse
import com.example.userauthservice.application.presentation.dto.SignInRequest
import com.example.userauthservice.application.presentation.dto.SignInResponse
import com.example.userauthservice.application.presentation.dto.SignUpRequest
import com.example.userauthservice.application.presentation.dto.SignUpResponse
import com.example.userauthservice.domain.user.Role
import com.example.userauthservice.shouldSameTime
import io.kotest.assertions.assertSoftly
import io.kotest.extensions.time.withConstantNow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class AuthControllerTest : FunctionalTestBase() {
    init {
        context("signUp") {
            test("유효한 이메일로 가입 시 사용자를 생성한다.") {
                // Given
                val now = LocalDateTime.now()

                val request =
                    SignUpRequest(
                        name = "testName",
                        email = "uniqueEmail@foobar.com",
                        password = "securePassword",
                    )

                // When
                val actual =
                    withConstantNow(now) {
                        client.postForEntity(
                            "/signup",
                            request,
                            SignUpResponse::class.java,
                        )
                    }

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id.shouldNotBeNull()
                    it.name shouldBe "testName"
                    it.email shouldBe "uniqueEmail@foobar.com"
                    it.role shouldBe "MEMBER"
                    it.createdAt shouldSameTime now
                    it.updatedAt shouldSameTime now
                }

                val actualUser = testHelper.getUser(actual.body!!.id)
                assertSoftly(actualUser) {
                    it.name shouldBe "testName"
                    it.email shouldBe "uniqueEmail@foobar.com"
                    it.role shouldBe Role.MEMBER
                    it.createdAt shouldSameTime now
                    it.updatedAt shouldSameTime now

                    testHelper.passwordMatches(
                        password = it.password,
                        expected = "securePassword",
                    ).shouldBeTrue()
                }
            }

            test("이미 사용된 이메일로 가입 시 오류를 반환한다.") {
                // Given
                val email = "duplicateEmail@foobar.com"

                testHelper.createUser(email = email)

                val request =
                    SignUpRequest(
                        name = "testName",
                        email = email,
                        password = "securePassword",
                    )

                // When
                val actual =
                    client.postForEntity(
                        "/signup",
                        request,
                        ErrorResponse::class.java,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.BAD_REQUEST

                assertSoftly(actual.body!!) {
                    it.status shouldBe HttpStatus.BAD_REQUEST.value()
                    it.error shouldBe "Bad Request"
                    it.message shouldBe "Email already exists"
                    it.path shouldBe "/api/signup"
                }
            }

            test("이메일 형식이 올바르지 않으면 오류를 반환한다.") {
                // Given
                val email = "invalid_email"

                val request =
                    SignUpRequest(
                        name = "testName",
                        email = email,
                        password = "securePassword",
                    )

                // When
                val actual =
                    client.postForEntity(
                        "/signup",
                        request,
                        ErrorResponse::class.java,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.BAD_REQUEST

                assertSoftly(actual.body!!) {
                    it.status shouldBe HttpStatus.BAD_REQUEST.value()
                    it.error shouldBe "Bad Request"
                    it.message shouldBe "Invalid email format"
                    it.path shouldBe "/api/signup"
                }
            }
        }

        context("signIn") {
            test("존재하는 이메일과 올바른 비밀번호가 주어지면 로그인에 성공한다.") {
                // Given
                val email = "signin-test@foobar.com"
                val password = "securePassword123"

                val user = testHelper.createUser(email = email, password = password)

                val request =
                    SignInRequest(
                        email = email,
                        password = password,
                    )

                // When
                val actual =
                    client.postForEntity(
                        "/signin",
                        request,
                        SignInResponse::class.java,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.accessToken.shouldNotBeNull()
                    it.refreshToken.shouldNotBeNull()
                    it.tokenType shouldBe "Bearer"
                    it.expiresIn shouldBe 900000L
                    it.user.email shouldBe email
                    it.user.role shouldBe "MEMBER"

                    testHelper.validateToken(it.accessToken) { principal ->
                        principal.userId shouldBe it.user.id
                        principal.email shouldBe email
                        principal.role shouldBe "MEMBER"
                        principal.tokenType shouldBe "ACCESS"
                    }
                    testHelper.validateToken(it.refreshToken) { principal ->
                        principal.userId shouldBe it.user.id
                        principal.email shouldBe email
                        principal.role shouldBe "MEMBER"
                        principal.tokenType shouldBe "REFRESH"
                    }
                }

                val actualRefreshToken = testHelper.getRefreshToken(actual.body!!.refreshToken)
                assertSoftly(actualRefreshToken) {
                    it.userId shouldBe user.id
                    it.token shouldBe actual.body!!.refreshToken
                    it.familyId.shouldNotBeNull()
                    it.isRevoked.shouldBeFalse()
                }
            }

            test("존재하지 않는 이메일로 로그인 시 오류를 반환한다.") {
                // Given
                val request =
                    SignInRequest(
                        email = "nonexistent@foobar.com",
                        password = "anyPassword",
                    )

                // When
                val actual =
                    client.postForEntity(
                        "/signin",
                        request,
                        ErrorResponse::class.java,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.UNAUTHORIZED

                assertSoftly(actual.body!!) {
                    it.status shouldBe HttpStatus.UNAUTHORIZED.value()
                    it.error shouldBe "Unauthorized"
                    it.message shouldBe "Invalid email or password"
                    it.path shouldBe "/api/signin"
                }
            }

            test("잘못된 비밀번호로 로그인 시 오류를 반환한다.") {
                // Given
                val email = "signin-password-test@foobar.com"

                testHelper.createUser(email = email)

                val request =
                    SignInRequest(
                        email = email,
                        password = "wrongPassword",
                    )

                // When
                val actual =
                    client.postForEntity(
                        "/signin",
                        request,
                        ErrorResponse::class.java,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.UNAUTHORIZED

                assertSoftly(actual.body!!) {
                    it.status shouldBe HttpStatus.UNAUTHORIZED.value()
                    it.error shouldBe "Unauthorized"
                    it.message shouldBe "Invalid email or password"
                    it.path shouldBe "/api/signin"
                }
            }

            test("이메일 형식이 올바르지 않으면 오류를 반환한다.") {
                // Given
                val request =
                    SignInRequest(
                        email = "invalid_email",
                        password = "anyPassword",
                    )

                // When
                val actual =
                    client.postForEntity(
                        "/signin",
                        request,
                        ErrorResponse::class.java,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.BAD_REQUEST

                assertSoftly(actual.body!!) {
                    it.status shouldBe HttpStatus.BAD_REQUEST.value()
                    it.error shouldBe "Bad Request"
                    it.message shouldBe "Invalid email format"
                    it.path shouldBe "/api/signin"
                }
            }
        }
    }
}
