package com.example.userauthservice.application.facade

import com.example.userauthservice.UnitTestBase
import com.example.userauthservice.domain.user.CreateUserData
import com.example.userauthservice.domain.user.Role
import com.example.userauthservice.domain.user.User
import com.example.userauthservice.domain.user.UserService
import com.example.userauthservice.generateEmail
import com.example.userauthservice.generateString
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

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
    }
}
