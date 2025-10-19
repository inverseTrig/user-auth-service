package com.example.userauthservice.domain.user

import com.example.userauthservice.ErrorMessage
import com.example.userauthservice.InvalidCredentialsException
import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.domain.user.event.UserDeletedEvent
import com.example.userauthservice.generateString
import com.example.userauthservice.shouldSameTime
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.extensions.time.withConstantNow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional

class UserServiceTest : UnitTestBase() {
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val eventPublisher: ApplicationEventPublisher = mockk()

    private val userService: UserService =
        UserService(
            userRepository = userRepository,
            passwordEncoder = passwordEncoder,
            eventPublisher = eventPublisher,
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

        context("getById") {
            test("ID로 사용자를 조회한다.") {
                // Given
                val user =
                    User(
                        name = "testName",
                        email = "test@email.com",
                        password = "encryptedPassword",
                        role = Role.MEMBER,
                    )

                every { userRepository.findById(user.id) } returns Optional.of(user)

                // When
                val result = userService.getById(user.id)

                // Then
                result shouldBe user
            }

            test("존재하지 않는 ID로 조회 시 오류를 던진다.") {
                // Given
                val userId = 999L

                every { userRepository.findById(userId) } returns Optional.empty()

                // Expect
                val exception =
                    shouldThrow<NoSuchElementException> {
                        userService.getById(userId)
                    }
                exception.message shouldBe ErrorMessage.NOT_FOUND.USER.message
            }
        }

        context("update") {
            test("사용자 정보를 업데이트한다.") {
                // Given
                val user =
                    User(
                        name = "originalName",
                        email = "original@email.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val updateData =
                    UpdateUserServiceData(
                        id = user.id,
                        name = "updatedName",
                        email = "updated@email.com",
                    )

                every { userRepository.findById(user.id) } returns Optional.of(user)
                every { userRepository.existsByEmailAndIdNot("updated@email.com", user.id) } returns false

                // When
                val result = userService.update(updateData)

                // Then
                result shouldBe user
                result.name shouldBe "updatedName"
                result.email shouldBe "updated@email.com"
            }

            test("존재하지 않는 사용자를 업데이트하면 오류를 던진다.") {
                // Given
                val userId = 999L
                val updateData =
                    UpdateUserServiceData(
                        id = userId,
                        name = generateString(),
                        email = generateString(),
                    )

                every { userRepository.findById(userId) } returns Optional.empty()

                // Expect
                val exception =
                    shouldThrow<NoSuchElementException> {
                        userService.update(updateData)
                    }
                exception.message shouldBe ErrorMessage.NOT_FOUND.USER.message
            }

            test("유효하지 않은 이메일 형식으로 업데이트하면 오류를 던진다.") {
                // Given
                val user =
                    User(
                        name = "originalName",
                        email = "original@email.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val updateData =
                    UpdateUserServiceData(
                        id = user.id,
                        name = "updatedName",
                        email = "invalid_email",
                    )

                every { userRepository.findById(user.id) } returns Optional.of(user)
                every { userRepository.existsByEmailAndIdNot("invalid_email", user.id) } returns false

                // Expect
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        userService.update(updateData)
                    }
                exception.message shouldBe ErrorMessage.INVALID.INVALID_EMAIL_FORMAT.message
            }

            test("이미 사용 중인 이메일로 업데이트하면 오류를 던진다.") {
                // Given
                val user =
                    User(
                        name = "originalName",
                        email = "original@email.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val updateData =
                    UpdateUserServiceData(
                        id = user.id,
                        name = "updatedName",
                        email = "existing@email.com",
                    )

                every { userRepository.findById(user.id) } returns Optional.of(user)
                every { userRepository.existsByEmailAndIdNot("existing@email.com", user.id) } returns true

                // Expect
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        userService.update(updateData)
                    }
                exception.message shouldBe ErrorMessage.INVALID.EMAIL_ALREADY_EXISTS.message
            }

            test("자신의 현재 이메일로 업데이트하면 성공한다.") {
                // Given
                val user =
                    User(
                        name = "originalName",
                        email = "same@email.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val updateData =
                    UpdateUserServiceData(
                        id = user.id,
                        name = "updatedName",
                        email = "same@email.com",
                    )

                every { userRepository.findById(user.id) } returns Optional.of(user)

                // When
                val result = userService.update(updateData)

                // Then
                result shouldBe user
                result.name shouldBe "updatedName"
                result.email shouldBe "same@email.com"
            }
        }

        context("getUsersByPage") {
            test("필터와 페이지 정보로 사용자 목록을 조회한다.") {
                // Given
                val user1 =
                    User(
                        name = "Alice",
                        email = "alice@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )
                val user2 =
                    User(
                        name = "Bob",
                        email = "bob@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val filter = UserFilter(email = "example.com")
                val pageable = PageRequest.of(0, 10)
                val expectedPage = PageImpl(listOf(user1, user2), pageable, 2)

                every { userRepository.findAll(filter, pageable) } returns expectedPage

                // When
                val result = userService.getUsersByPage(filter, pageable)

                // Then
                result shouldBe expectedPage

                verify { userRepository.findAll(filter, pageable) }
            }
        }

        context("deleteById") {
            test("ID로 사용자를 삭제하고 사용자 삭제 이벤트를 발행한다.") {
                // Given
                val now = LocalDateTime.now()

                val user =
                    User(
                        name = "UserToDelete",
                        email = "delete@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                every { userRepository.findById(user.id) } returns Optional.of(user)

                // When
                withConstantNow(now) {
                    userService.deleteById(user.id)
                }

                // Then
                user.deletedAt shouldSameTime now

                verify {
                    val matcher =
                        withArg<UserDeletedEvent> {
                            it.userId shouldBe user.id
                            it.email shouldBe "delete@example.com"
                            it.name shouldBe "UserToDelete"
                        }
                    eventPublisher.publishEvent(matcher)
                }
            }
        }
    }
}
