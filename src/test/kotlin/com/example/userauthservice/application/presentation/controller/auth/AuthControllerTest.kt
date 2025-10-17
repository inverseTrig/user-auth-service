package com.example.userauthservice.application.presentation.controller.auth

import com.example.userauthservice.FunctionalTestBase
import com.example.userauthservice.application.presentation.ErrorResponse
import com.example.userauthservice.application.presentation.dto.auth.CreateUserRequest
import com.example.userauthservice.application.presentation.dto.auth.UserResponse
import com.example.userauthservice.domain.user.Role
import com.example.userauthservice.shouldSameTime
import io.kotest.assertions.assertSoftly
import io.kotest.extensions.time.withConstantNow
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
                    CreateUserRequest(
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
                            UserResponse::class.java,
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
                    CreateUserRequest(
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
                    CreateUserRequest(
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
    }
}
