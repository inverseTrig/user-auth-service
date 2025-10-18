package com.example.userauthservice.application.presentation.controller.user

import com.example.userauthservice.FunctionalTestBase
import com.example.userauthservice.application.presentation.ErrorResponse
import com.example.userauthservice.application.presentation.dto.UserResponse
import com.example.userauthservice.domain.user.Role
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus

class UserControllerTest : FunctionalTestBase() {
    init {
        context("getById") {
            test("ADMIN 권한으로 본인의 정보를 조회할 수 있다.") {
                // Given
                val adminUser =
                    testHelper.createUser(
                        name = "AdminTest",
                        email = "admin-get-by-id@example.com",
                        role = Role.ADMIN,
                    )

                val token = testHelper.generateToken(adminUser)

                // When
                val actual =
                    client.getForEntity(
                        "/users/${adminUser.id}",
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id shouldBe adminUser.id
                    it.name shouldBe "AdminTest"
                    it.email shouldBe "admin-get-by-id@example.com"
                    it.role shouldBe "ADMIN"
                }
            }

            test("ADMIN 권한으로 다른 사용자의 정보를 조회할 수 있다.") {
                // Given
                val adminUser = testHelper.createUser(role = Role.ADMIN)

                val memberUser =
                    testHelper.createUser(
                        name = "AnotherMember",
                        email = "member-to-view@example.com",
                        role = Role.MEMBER,
                    )

                val token = testHelper.generateToken(adminUser)

                // When
                val actual =
                    client.getForEntity(
                        "/users/${memberUser.id}",
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id shouldBe memberUser.id
                    it.name shouldBe "AnotherMember"
                    it.email shouldBe "member-to-view@example.com"
                    it.role shouldBe "MEMBER"
                }
            }

            test("MEMBER 권한으로 본인의 정보를 조회할 수 있다.") {
                // Given
                val memberUser =
                    testHelper.createUser(
                        name = "MemberTest",
                        email = "member-self@test.com",
                        role = Role.MEMBER,
                    )

                val token = testHelper.generateToken(memberUser)

                // When
                val actual =
                    client.getForEntity(
                        "/users/${memberUser.id}",
                        UserResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.OK

                assertSoftly(actual.body!!) {
                    it.id shouldBe memberUser.id
                    it.name shouldBe "MemberTest"
                    it.email shouldBe "member-self@test.com"
                    it.role shouldBe "MEMBER"
                }
            }

            test("MEMBER 권한으로 다른 사용자의 정보를 조회하면 오류를 발생한다.") {
                // Given
                val memberUser = testHelper.createUser(role = Role.MEMBER)
                val otherMember = testHelper.createUser(role = Role.MEMBER)

                val token = testHelper.generateToken(memberUser)

                // When
                val actual =
                    client.getForEntity(
                        "/users/${otherMember.id}",
                        ErrorResponse::class,
                        token = token,
                    )

                // Then
                actual.statusCode shouldBe HttpStatus.FORBIDDEN

                assertSoftly(actual.body!!) {
                    it.status shouldBe HttpStatus.FORBIDDEN.value()
                    it.error shouldBe "Forbidden"
                    it.message shouldBe "Access Denied"
                    it.path shouldBe "/api/users/${otherMember.id}"
                }
            }
        }
    }
}
