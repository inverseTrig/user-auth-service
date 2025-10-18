package com.example.userauthservice.domain.user

import com.example.userauthservice.ErrorMessage
import com.example.userauthservice.InvalidCredentialsException
import com.example.userauthservice.UnitTestBase
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

        context("authenticate") {
            test("유효한 자격 증명으로 사용자를 인증한다.") {
                // Given
                val email = "test@email.com"
                val password = "securePassword"
                val encryptedPassword = "encryptedPassword"
                val user =
                    User(
                        name = "testName",
                        email = email,
                        password = encryptedPassword,
                        role = Role.MEMBER,
                    )

                every { userRepository.findByEmail(email) } returns user
                every { passwordEncoder.matches(password, encryptedPassword) } returns true

                // When
                val result = userService.authenticate(email, password)

                // Then
                result shouldBe user
                verify {
                    userRepository.findByEmail(email)
                    passwordEncoder.matches(password, encryptedPassword)
                }
            }

            test("존재하지 않는 이메일로 인증을 시도하면 오류를 던진다.") {
                // Given
                val email = "nonexistent@email.com"
                val password = "password"

                every { userRepository.findByEmail(email) } returns null

                // Expect
                val exception =
                    shouldThrow<InvalidCredentialsException> {
                        userService.authenticate(email, password)
                    }
                exception.message shouldBe ErrorMessage.INVALID.INVALID_CREDENTIALS.message
            }

            test("잘못된 비밀번호로 인증을 시도하면 오류를 던진다.") {
                // Given
                val email = "test@email.com"
                val password = "wrongPassword"
                val encryptedPassword = "encryptedPassword"
                val user =
                    User(
                        name = "testName",
                        email = email,
                        password = encryptedPassword,
                        role = Role.MEMBER,
                    )

                every { userRepository.findByEmail(email) } returns user
                every { passwordEncoder.matches(password, encryptedPassword) } returns false

                // Expect
                val exception =
                    shouldThrow<InvalidCredentialsException> {
                        userService.authenticate(email, password)
                    }
                exception.message shouldBe ErrorMessage.INVALID.INVALID_CREDENTIALS.message
            }
        }
    }
}
