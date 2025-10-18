package com.example.userauthservice.application.facade

import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.domain.user.CreateUserData
import com.example.userauthservice.domain.user.Role
import com.example.userauthservice.domain.user.UpdateUserServiceData
import com.example.userauthservice.domain.user.User
import com.example.userauthservice.domain.user.UserFilter
import com.example.userauthservice.domain.user.UserService
import com.example.userauthservice.generateEmail
import com.example.userauthservice.generateString
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class UserFacadeTest : UnitTestBase() {
    private val userService: UserService = mockk()
    private val userFacade =
        UserFacade(
            userService = userService,
        )

    init {
        context("createUser") {
            test("주어진 정보로 사용자를 생성한다") {
                // Given
                val user =
                    User(
                        name = generateString(),
                        email = generateEmail(),
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val data =
                    CreateUserData(
                        name = "name",
                        email = "email@example.com",
                        password = "securePassword",
                        role = Role.ADMIN,
                    )

                every { userService.create(any()) } returns user

                // When
                val actual = userFacade.createUser(data)

                // Then
                actual shouldBe user

                verify {
                    val matcher =
                        withArg<CreateUserData> {
                            it.name shouldBe "name"
                            it.email shouldBe "email@example.com"
                            it.password shouldBe "securePassword"
                            it.role shouldBe Role.ADMIN
                        }
                    userService.create(matcher)
                }
            }
        }

        context("getUserById") {
            test("ID로 사용자를 조회한다") {
                // Given
                val user =
                    User(
                        name = generateString(),
                        email = generateEmail(),
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                every { userService.getById(user.id) } returns user

                // When
                val actual = userFacade.getUserById(user.id)

                // Then
                actual shouldBe user
            }
        }

        context("updateUser") {
            test("사용자 정보를 업데이트한다") {
                // Given
                val user =
                    User(
                        name = "updatedName",
                        email = "updated@example.com",
                        password = generateString(),
                        role = Role.MEMBER,
                    )

                val data =
                    UpdateUserServiceData(
                        id = user.id,
                        name = "updatedName",
                        email = "updated@example.com",
                    )

                every { userService.update(any()) } returns user

                // When
                val actual = userFacade.updateUser(data)

                // Then
                actual shouldBe user

                verify {
                    val matcher =
                        withArg<UpdateUserServiceData> {
                            it.id shouldBe user.id
                            it.name shouldBe "updatedName"
                            it.email shouldBe "updated@example.com"
                        }
                    userService.update(matcher)
                }
            }
        }

        context("getUsersByPage") {
            test("필터와 페이지 정보로 사용자 목록을 조회한다") {
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

                val filter = UserFilter(email = "example")
                val pageable = PageRequest.of(0, 10)
                val expectedPage = PageImpl(listOf(user1, user2), pageable, 2)

                every { userService.getUsersByPage(filter, pageable) } returns expectedPage

                // When
                val actual = userFacade.getUsersByPage(filter, pageable)

                // Then
                actual shouldBe expectedPage

                verify { userService.getUsersByPage(filter, pageable) }
            }
        }

        context("deleteUser") {
            test("사용자 ID로 사용자를 삭제한다") {
                // Given
                val userId = 123L

                // When
                userFacade.deleteUser(userId)

                // Then
                verify { userService.deleteById(userId) }
            }
        }
    }
}
