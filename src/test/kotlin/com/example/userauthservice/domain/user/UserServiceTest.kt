package com.example.userauthservice.domain.user

import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.domain.ErrorMessage
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceTest : UnitTestBase() {
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()

    private val userService: UserService =
        UserService(
            userRepository = userRepository,
            passwordEncoder = passwordEncoder,
        )

    init {
        context("create") {
            test("주어진 정보로 사용자를 생성한다.") {
                // Given
                val createUserData =
                    CreateUserData(
                        name = "testName",
                        email = "test@email.com",
                        password = "securePassword",
                        role = Role.ADMIN,
                    )

                every { userRepository.existsByEmail("test@email.com") } returns false
                every { passwordEncoder.encode("securePassword") } returns "encryptedPassword"
                every { userRepository.save(any()) } returnsArgument 0

                // When
                userService.create(createUserData)

                // Then
                verify {
                    val matcher =
                        withArg<User> {
                            it.name shouldBe "testName"
                            it.email shouldBe "test@email.com"
                            it.password shouldBe "encryptedPassword"
                            it.role shouldBe Role.ADMIN
                        }
                    userRepository.save(matcher)
                }
            }

            test("주어진 이메일로 이미 사용자가 존재하면 오류를 던진다.") {
                // Given
                val createUserData =
                    CreateUserData(
                        name = "testName",
                        email = "test@email.com",
                        password = "securePassword",
                        role = Role.ADMIN,
                    )

                every { userRepository.existsByEmail("test@email.com") } returns true

                // Expect
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        userService.create(createUserData)
                    }
                exception.message shouldBe ErrorMessage.INVALID.EMAIL_ALREADY_EXISTS.message
            }
        }
    }
}
