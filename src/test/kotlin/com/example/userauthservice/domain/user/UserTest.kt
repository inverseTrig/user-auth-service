package com.example.userauthservice.domain.user

import com.example.userauthservice.ErrorMessage
import com.example.userauthservice.UnitTestBase
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

class UserTest : UnitTestBase() {
    init {
        context("init") {
            test("이메일의 형식이 올바르면 예외가 발생하지 않는다") {
                // Given
                val email = "valid_email@example.com"

                // When
                val user =
                    User(
                        name = "testName",
                        email = email,
                        password = "secure_password",
                        role = Role.MEMBER,
                    )

                // Then
                assertSoftly(user) {
                    it.name shouldBe "testName"
                    it.email shouldBe "valid_email@example.com"
                    it.password shouldBe "secure_password"
                    it.role shouldBe Role.MEMBER
                }
            }

            test("이메일의 형식이 올바르지 않으면 예외를 던진다.") {
                // Given
                val email = "invalid_email"

                // Expect
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        User(
                            name = "testName",
                            email = email,
                            password = "secure_password",
                            role = Role.MEMBER,
                        )
                    }
                exception.message shouldBe ErrorMessage.INVALID.INVALID_EMAIL_FORMAT.message
            }
        }
    }
}
