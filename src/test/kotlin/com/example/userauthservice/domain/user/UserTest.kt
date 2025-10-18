package com.example.userauthservice.domain.user

import com.example.userauthservice.ErrorMessage
import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.generateString
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

        context("update") {
            test("모든 필드를 업데이트한다") {
                // Given
                val user =
                    User(
                        name = "originalName",
                        email = "original@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val updateData =
                    UpdateUserData(
                        name = "updatedName",
                        email = "updated@example.com",
                    )

                // When
                user.update(updateData)

                // Then
                assertSoftly(user) {
                    it.name shouldBe "updatedName"
                    it.email shouldBe "updated@example.com"
                }
            }

            test("이름만 업데이트한다") {
                // Given
                val user =
                    User(
                        name = "originalName",
                        email = "original@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val updateData =
                    UpdateUserData(
                        name = "updatedName",
                        email = null,
                    )

                // When
                user.update(updateData)

                // Then
                assertSoftly(user) {
                    it.name shouldBe "updatedName"
                    it.email shouldBe "original@example.com"
                }
            }

            test("이메일만 업데이트한다") {
                // Given
                val user =
                    User(
                        name = "originalName",
                        email = "original@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val updateData =
                    UpdateUserData(
                        name = null,
                        email = "updated@example.com",
                    )

                // When
                user.update(updateData)

                // Then
                assertSoftly(user) {
                    it.name shouldBe "originalName"
                    it.email shouldBe "updated@example.com"
                }
            }

            test("모든 값이 null이면 아무것도 업데이트하지 않는다") {
                // Given
                val user =
                    User(
                        name = "originalName",
                        email = "original@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val updateData =
                    UpdateUserData(
                        name = null,
                        email = null,
                    )

                // When
                user.update(updateData)

                // Then
                assertSoftly(user) {
                    it.name shouldBe "originalName"
                    it.email shouldBe "original@example.com"
                }
            }

            test("유효하지 않은 이메일 형식으로 업데이트하면 예외를 던진다") {
                // Given
                val user =
                    User(
                        name = "originalName",
                        email = "original@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val updateData =
                    UpdateUserData(
                        name = null,
                        email = "invalid_email",
                    )

                // Expect
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        user.update(updateData)
                    }
                exception.message shouldBe ErrorMessage.INVALID.INVALID_EMAIL_FORMAT.message
            }
        }
    }
}
